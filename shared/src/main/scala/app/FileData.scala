package app

import upickle.default.{macroRW, ReadWriter => RW}

case class SentimentCounter(pos: Int, neg: Int, neu: Int)
object SentimentCounter {
  implicit val rw: RW[SentimentCounter] = macroRW
}

case class StockSentiment(ticker: String, pos: Int, neg: Int, neu: Int)
object StockSentiment {
  implicit val rw: RW[StockSentiment] = macroRW
}

case class StockSentiments(tickers: Array[String], sentiment: SentimentCounter)
object StockSentiments {
  implicit val rw: RW[StockSentiments] = macroRW
}
