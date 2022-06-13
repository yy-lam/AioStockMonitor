package app

import akka.{Done, NotUsed}
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server._
import akka.routing.BalancingPool
import akka.stream.{ActorMaterializer, CompletionStrategy, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import edu.stanford.nlp.pipeline.StanfordCoreNLP

import java.util.Properties
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.util.{Failure, Success}

class ServerActor(streamingActor: ActorRef) extends Actor {
  import Classifier._

  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)
  val sentimentAnalyzer = new SentimentAnalyzer(pipeline)
  val classifiers = context.actorOf(BalancingPool(5).props(Classifier.props(sentimentAnalyzer).withDispatcher("resizable-thread-pool")), "master")
  val consumer = context.actorOf(SocketConsumer.props(9000, classifiers), s"consumer")

  override def receive: Receive = {
    case TaskResult(tickers: Array[String], sentiments: SentimentCounter) =>
      // send ws message to ws server
      tickers.foreach(ticker => streamingActor ! StockSentiment(ticker, sentiments.pos, sentiments.neg, sentiments.neu))
  }
}

object Router extends autowire.Server[String, upickle.default.Reader, upickle.default.Writer]{
  def read[Result: upickle.default.Reader](p: String) = upickle.default.read[Result](p)
  def write[Result: upickle.default.Writer](r: Result) = upickle.default.write(r)
}

object AkkaHttpServer extends App with SentimentApi with LazyLogging {
  override def list(path: String) = {
    val (dir, last) = path.splitAt(path.lastIndexOf("/") + 1)
    val files =
      Option(new java.io.File("./" + dir).listFiles())
        .toSeq.flatten
    for {
      f <- files
      if f.getName.startsWith(last)
    } yield StockSentiment("placeholder", 0, 0, 0)
  }

  implicit val system = ActorSystem("akka-http-server")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

//  val routes: Route = {
//    concat(
//      get {
//        pathSingleSlash {
//          complete(HttpEntity(
//            ContentTypes.`text/html(UTF-8)`,
//            Page.skeleton.render))
//        } ~ getFromResourceDirectory("")
//      }
//    )
//  }


  val (streamingActor, sseSource) =
    Source.actorRef[StockSentiment](20, akka.stream.OverflowStrategy.dropTail)
      .map(s => ServerSentEvent(upickle.default.write[StockSentiment](s)))
      .keepAlive(1.second, () => ServerSentEvent.heartbeat)
      .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
      .run()

  val serverActor = system.actorOf(Props(new ServerActor(streamingActor)), "server")

  def route: Route = {
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
//    implicit val RW = upickle.default.macroRW[StockSentiment]
    path("") {
      get {
        complete {
          sseSource
        }
      }
    }
  }


  val futureBinding = Http().newServerAt("localhost", 8080).bind(route)
  futureBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }

  StdIn.readLine() // let it run until user presses return
  futureBinding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}