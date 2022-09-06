package app

import org.scalajs.dom
import org.scalajs.dom.EventSource
import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.web.html._


@react object Layout {
  type Props = Unit

  def updateSentiment(oldSent: SentimentCounter, newSent: SentimentCounter): SentimentCounter =
    SentimentCounter(oldSent.pos + newSent.pos, oldSent.neg + oldSent.neg, oldSent.neu + newSent.neu)

  val component: FunctionalComponent[Props] = FunctionalComponent[Props] { props =>
    val (state, updateState) = useState(Map[String, SentimentCounter]())

    useEffect(
      () => {
        val sse = new EventSource("news")
        sse.onopen = e => println("SSE is open")
        sse.addEventListener("newEntry", (e: dom.MessageEvent) =>
          if (e != null && e.data != null && e.data.toString.nonEmpty) {
            val stockSentiment = upickle.default.read[StockSentiment](e.data.toString)
            val ticker = stockSentiment.ticker
            val (pos, neg, neu) = (stockSentiment.pos, stockSentiment.neg, stockSentiment.neu)

            if (state contains stockSentiment.ticker) {
              updateState(state + (ticker -> updateSentiment(state(ticker), SentimentCounter(pos, neg, neu))))
            } else {
              updateState(state + (ticker -> SentimentCounter(pos, neg, neu)))
            }
            println(state)
          }
        )
        () => sse.close()
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
