package app

import slinky.core.Component
import slinky.core.annotations.react
import slinky.web.html._

import scala.scalajs.js

@react class Panel extends Component {
  case class Props(tick: String, sent: SentimentCounter)
  case class State(pos: Int, neg: Int, neu: Int)
  def initialState = State(props.sent.pos, props.sent.neg, props.sent.neu)

  def render() = {
    div(style := js.Dynamic.literal(border = "1px solid black"))(
      h1 (props.tick),
      p (s"Positive: ${state.pos} | Negative: ${state.neg} | Neutral: ${state.neu}")
    )
  }
//  val component = FunctionalComponent[Props] { props =>
//    val (state, updateState) = useState(props.initialSent)
//
//    div(style := js.Dynamic.literal(border = "1px solid black"))(
//      h1 (props.tick),
//      p (s"Positive: ${state.pos} | Negative: ${state.neg} | Neutral: ${state.neu}")
//    )
//  }
}
