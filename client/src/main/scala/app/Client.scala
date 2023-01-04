package app

import org.scalajs.dom._
import slinky.web.ReactDOM


//@JSExportTopLevel("app")
object Client {
//  @JSExport
  def main(args: Array[String]): Unit = {
    println("Hello ScalaJS")

    ReactDOM.render(
      Layout(),
      document.getElementById("contents")
    )
  }
}


