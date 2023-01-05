package app

import org.scalajs.dom
import org.scalajs.dom.EventSource
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.web.html._

@react object Layout {
  type Props = Unit
  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { props =>
    val (state, updateState) = useState(Map[String, SentimentCounter]())
    val sse = new EventSource("news")

    useEffect(
      () => {
        sse.addEventListener("newEntry", (e: dom.MessageEvent) =>
          if (e != null && e.data != null && e.data.toString.nonEmpty) {
            val stockSentiments = upickle.default.read[StockSentiments](e.data.toString)
            val (pos, neg, neu) = (stockSentiments.sentiment.pos, stockSentiments.sentiment.neg, stockSentiments.sentiment.neu)
            updateState(state => {
              state ++ (for (ticker <- stockSentiments.tickers) yield ticker -> SentimentCounter(pos, neg, neu))
            })
          }
        , false)
        sse.addEventListener("error", (e: dom.MessageEvent) => {
          if (sse.readyState == EventSource.CLOSED) {
            println(s"Event was closed: $e")
          }
        }, false);
      }, Seq(state)
    )

    div(
      h1 ("Sentiment Monitor"),
      ul (
        for ((stock: String, sentiments: SentimentCounter) <- state)
          yield li(key := stock)(Panel(tick = stock, sent = sentiments))
      )
    )
  }
}
