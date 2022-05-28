import akka.actor.{Actor, ActorLogging}

object Classifier {
  case class TcpMessage(tickers: List[String], headline: String)
  case class ClassificationTask(tickers: List[String], headline: String)
}
class Classifier extends Actor with ActorLogging {
  import Classifier._
  import Sentiment._
  val sentimentAnalyzer = new SentimentAnalyzer()

  override def postStop(): Unit = log.info(s"[${self.path.name}] stops")

  override def receive: Receive = {
    case message: String =>
      val rawText = message.split('|')
      try {
        val tickers = rawText(0)
        val paragraph = rawText(1)
        val sentiments = sentimentAnalyzer.extractSentiments(paragraph).foldLeft(Sentiments(0, 0, 0))((acc, pair) =>
          pair._2 match {
            case POSITIVE => acc.copy(pos=acc.pos+1)
            case NEGATIVE => acc.copy(neg=acc.neg+1)
            case NEUTRAL => acc.copy(neu=acc.neu+1)
          })
        log.info(s"[${self.path.name}] ${sentiments.toString}")
      } catch {
        case e: Any => log.error(s"error parsing $message: $e")
      }
    case _ => log.warning(s"[${self.path.name}] receives garbage")
  }
}