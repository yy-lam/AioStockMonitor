package app

import org.scalajs.dom
import org.scalajs.dom._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._


//@JSExportTopLevel("app")
object Client {
//  @JSExport
  def main(args: Array[String]): Unit = {
    println("Hello ScalaJS")

//    val sse = new EventSource("news")
//        sse.onmessage = {
//          e: dom.MessageEvent => {
//            val msgElem = dom.document.getElementById("contents")
//            if (e != null && e.data != null) {
//              val stockSentiment = upickle.default.read[StockSentiment](e.data.toString)
//              val existedDiv = dom.document.getElementById(stockSentiment.ticker)
//              if (existedDiv == null) {
//                msgElem.appendChild(div(id:=stockSentiment.ticker,
//                  li(s"Ticker: ${stockSentiment.ticker}, " +
//                    s"Pos: ${stockSentiment.pos}, " +
//                    s"Neg: ${stockSentiment.neg}, " +
//                    s"Neu: ${stockSentiment.neu}")).render)
//              } else {
//                println("update values!")
//                existedDiv.firstChild.nodeValue = "new"
//              }
//            }
//          }
//        }

    ReactDOM.render(
      Layout(),
      document.getElementById("contents")
    )

  }
}


