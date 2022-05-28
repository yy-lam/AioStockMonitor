import org.scalajs.dom
import dom.{document, html}

import scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel
import scalatags.Text.all._


object App extends {
  def main(args: Array[String]): Unit = {
    appendPar(document.body, "Hello World")
  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }

  @JSExportTopLevel("addClickedMessage")
  def addClickedMessage(): Unit = {
    appendPar(document.body, "You clicked the button!")
  }
}
