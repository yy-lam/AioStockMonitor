package app

import scalatags.Text.all._

object Page {
//  val boot =
//    "app.main(document.getElementById('contents'))"
  val skeleton =
    html(
      head(
        script(src:="/main-bundle.js"),
        script(src:="/app-fastopt-bundle.js"),
        link(
          rel:="stylesheet",
          href:="https://cdnjs.cloudflare.com/ajax/libs/pure/0.5.0/pure-min.css"
        )
      ),
      body(
//        onload:=boot,
        div(id:="contents")
      )
    )
}