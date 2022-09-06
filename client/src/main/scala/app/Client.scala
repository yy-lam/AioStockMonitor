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

    ReactDOM.render(
      Layout(),
      document.getElementById("contents")
    )
  }
}


