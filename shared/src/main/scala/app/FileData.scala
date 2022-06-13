package app

import upickle.default.{macroRW, ReadWriter => RW}

case class FileData(name: String, size: Long)
object FileData {
  implicit val rw: RW[FileData] = macroRW
}

trait DataApi {
  def list(path: String): Seq[FileData]
}

//case class StocksSentiments(stocks: Array[String], sentiments: StocksSentiments)
case class StockSentiment(ticker: String, pos: Int, neg: Int, neu: Int)
object StockSentiment {
  implicit val rw: RW[StockSentiment] = macroRW
}

trait SentimentApi {
  def list(path: String): Seq[StockSentiment]
}