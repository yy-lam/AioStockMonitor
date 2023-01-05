package app

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.io.Tcp.Abort
import akka.routing.BalancingPool
import akka.stream.{ActorMaterializer, DelayOverflowStrategy, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source, SourceQueueWithComplete}
import com.typesafe.scalalogging.LazyLogging
import edu.stanford.nlp.pipeline.StanfordCoreNLP

import java.util.Properties
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.util.{Failure, Success}
import scala.collection.mutable.HashMap

case class Stop()
class ServerActor(sourceQueue: SourceQueueWithComplete[StockSentiments]) extends Actor {
  import Classifier._
  implicit lazy val ec = context.dispatcher

  var stockSentiments = HashMap[String, SentimentCounter]()
  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)
  val sentimentAnalyzer = new SentimentAnalyzer(pipeline)
  val classifiers: ActorRef = context.actorOf(BalancingPool(6).props(Classifier.props(sentimentAnalyzer)), "master")
  val consumer: ActorRef = context.actorOf(SocketConsumer.props(9000, classifiers), "consumer")

  override def receive: Receive = {
    case TaskResult(tickers: Array[String], sentiment: SentimentCounter) =>
      sourceQueue.offer(StockSentiments(tickers, sentiment))
    case Stop => consumer ! Abort
  }
}

object AkkaHttpServer extends App with LazyLogging {
  implicit val system = ActorSystem("aio-stock-monitor")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  lazy val (sourceQueue, sseSource) = Source.queue[StockSentiments](Int.MaxValue, OverflowStrategy.backpressure)
    .delay(1.seconds, DelayOverflowStrategy.backpressure)
    .map(s => ServerSentEvent(upickle.default.write[StockSentiments](s), Some("newEntry")))
//    .keepAlive(1.second, () => ServerSentEvent.heartbeat)
    .toMat(BroadcastHub.sink[ServerSentEvent])(Keep.both)
    .run()

  val serverActor = system.actorOf(Props(new ServerActor(sourceQueue)), "server")

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
      serverActor ! Stop
      system.terminate()
  }

  StdIn.readLine() // let it run until user presses return
  futureBinding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}