package app

import scalatags.JsDom.all._
import org.scalajs.dom
import dom.html

import scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scalajs.concurrent.JSExecutionContext.Implicits.queue
import autowire._


object Ajaxer extends autowire.Client[String, upickle.default.Reader, upickle.default.Writer]{
  override def doCall(req: Request) = {
    dom.ext.Ajax.post(
      url = "/news",
      data = upickle.default.write(req.args)
    ).map(_.responseText)
  }

  def read[Result: upickle.default.Reader](p: String) = upickle.default.read[Result](p)
  def write[Result: upickle.default.Writer](r: Result) = upickle.default.write(r)
}

@JSExportTopLevel("app")
object Client {
  @JSExport
  def main(container: html.Div) = {
    val inputBox = input.render
    val outputBox = ul.render
    def update() = {

    }
    container.appendChild(
      div(
        h1("Hello world"),
      ).render
    )
  }
}