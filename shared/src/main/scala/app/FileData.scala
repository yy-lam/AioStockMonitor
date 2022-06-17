package app

import upickle.default.{macroRW, ReadWriter => RW}

//case class StocksSentiments(stocks: Array[String], sentiments: StocksSentiments)
case class StockSentiment(ticker: String, pos: Int, neg: Int, neu: Int)
object StockSentiment {
  implicit val rw: RW[StockSentiment] = macroRW
}
