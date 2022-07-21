package app

import scalatags.Text.all._

object Page {
  val boot =
    "app.main()"
  val skeleton =
    html(
      head(
        link(
          rel:="stylesheet",
          href:="https://cdnjs.cloudflare.com/ajax/libs/pure/0.5.0/pure-min.css"
        )
      ),
      body(
        div(id:="contents"),
        script(src:="/scalajs-bundler/app-fastopt-bundle.js")
      )
    )
}