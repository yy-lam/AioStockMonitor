package app

import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._

import scala.scalajs.js

@react class Panel extends Component {
  case class Props(tick: String)
  case class State(pos: Int, neg: Int, neu: Int)

  def initialState = State(0, 0, 0)

  def render() = {
    div(style := js.Dynamic.literal(border = "1px solid black"))(
      h1 (props.tick),
      p (s"Positive: ${state.pos} | Negative: ${state.neg} | Neutral: ${state.neu}")
    )
  }

}
