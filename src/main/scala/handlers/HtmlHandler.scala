package handlers

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import scalatags.Text.all.{head => htmlHead, title => _, _}
import scalatags.Text.tags2.title

def renderHelloPage(name: String): String =
  html(
    htmlHead(title(s"Hello, $name")),
    body(
      h1(s"Hello, $name!"),
      p("Welcome to the site."),
      button(attr("hx-get") := "/ping", attr("hx-swap") := "outerHTML")("Ping the server!")
    )
  ).render

object HtmlHandler:
  def routes: Route = concat(
    path("hello" / Segment) { name =>
      val htmlResponse = renderHelloPage(name)
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, htmlResponse))
    }
  )
