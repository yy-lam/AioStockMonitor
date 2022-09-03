package app

import akka.actor.{Actor, ActorLogging, Props}

object Classifier {
  case class TaskResult(tickers: Array[String], counter: SentimentCounter)
  def props(sentimentAnalyzer: SentimentAnalyzer): Props = Props(new Classifier(sentimentAnalyzer))
}
class Classifier(sentimentAnalyzer: SentimentAnalyzer) extends Actor with ActorLogging {
  import Classifier._
  import Sentiment._

  override def postStop(): Unit = log.info(s"[${self.path.name}] stops")

  override def receive: Receive = {
    case message: String =>
      val rawText = message.split('|')
      try {
        val tickers = rawText(0)
        val paragraph = rawText(1)
        val sentiments = sentimentAnalyzer.extractSentiments(paragraph).foldLeft(SentimentCounter(0, 0, 0))((acc, sentiment) =>
          sentiment match {
            case POSITIVE => SentimentCounter(acc.pos+1, acc.neg, acc.neu)
            case NEGATIVE => SentimentCounter(acc.pos, acc.neg+1, acc.neu)
            case NEUTRAL  => SentimentCounter(acc.pos, acc.neg, acc.neu+1)
          })
        context.actorSelection("/user/server") ! TaskResult(tickers.split(", "), sentiments)
      } catch {
        case e: Any => log.error(s"error parsing $message: $e")
      }
    case _ => log.warning(s"[${self.path.name}] receives garbage")
  }
}