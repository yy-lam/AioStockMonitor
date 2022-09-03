package app

import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.web.html._

@react class Layout extends StatelessComponent {
  type Props = Unit

  def render() = {
    div(
      h1 ("Sentiment Monitor"),
      Panel("TEST")
    )
  }
}
