package app

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.routing.BalancingPool
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import com.typesafe.scalalogging.LazyLogging
import edu.stanford.nlp.pipeline.StanfordCoreNLP

import java.util.Properties
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.util.{Failure, Success}
import scala.collection.mutable.HashMap

class ServerActor(streamingActor: ActorRef) extends Actor {
  import Classifier._
  var stockSentiments = HashMap[String, SentimentCounter]()
  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)
  val sentimentAnalyzer = new SentimentAnalyzer(pipeline)
  val classifiers: ActorRef = context.actorOf(BalancingPool(3).props(Classifier.props(sentimentAnalyzer).withDispatcher("resizable-thread-pool")), "master")
  val consumer: ActorRef = context.actorOf(SocketConsumer.props(9000, classifiers), s"consumer")

  override def receive: Receive = {
    case TaskResult(tickers: Array[String], sentiments: SentimentCounter) =>
      tickers.foreach(ticker => stockSentiments.getOrElse(ticker, SentimentCounter(0, 0, 0)) match {
        case counter => stockSentiments(ticker) = SentimentCounter(counter.pos + sentiments.pos, counter.neg + sentiments.neg, counter.neu + sentiments.neu)
      })
      stockSentiments.foreachEntry((ticker, sentiment) => streamingActor ! StockSentiment(ticker, sentiment.pos, sentiment.neg, sentiment.neu))
  }
}

object AkkaHttpServer extends App with LazyLogging {
  implicit val system = ActorSystem("aio-stock-monitor")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val (streamingActor, sseSource) =
    Source.actorRef[StockSentiment](256, akka.stream.OverflowStrategy.dropHead)
      .map(s => ServerSentEvent(upickle.default.write[StockSentiment](s), "newEntry"))
      .keepAlive(1.second, () => ServerSentEvent.heartbeat)
      .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
      .run()

  val serverActor = system.actorOf(Props(new ServerActor(streamingActor)), "server")

  val sseRoute: Route = {
    import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
    path("news") {
      get {
        complete {
          sseSource
        }
      }
    }
  }

  val routes: Route =
    pathSingleSlash {
      get {
        complete {
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            Page.skeleton.render)
        }
      }
    } ~
      pathPrefix("assets" / Remaining) { file =>
        // optionally compresses the response with Gzip or Deflate
        // if the client accepts compressed responses
        encodeResponse {
          getFromResource("public/" + file)
        }
      }

  val futureBinding = Http().newServerAt("localhost", 8080).bind(routes ~ sseRoute)
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