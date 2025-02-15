package handlers

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import scalatags.Text.all.{head => htmlHead, title => _, _}
import scalatags.Text.tags2.title

def layout(pageTitle: String)(content: Modifier*): String =
  html(
    htmlHead(
      title(pageTitle),
      meta(charset := "UTF-8"),
      link(rel := "stylesheet", href := "/assets/style.css")
    ),
    body(
      h1("My Cool Website"),
      div(content), // Content provided by the route
      footer(
        p("© 2025 My Cool Website")
      )
    )
  ).render

def renderHelloPage(name: String): String =
  layout(s"Hello $name")(
    h2(s"Hello, $name!"),
    p("Welcome to our website."),
    button(
      attr("hx-get") := "/ping",
      attr("hx-swap") := "outerHTML"
    )("Ping the server!")
  )

extension (content: String)
  def toUtf8Http: Route = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, content))

object HtmlHandler:
  def routes: Route = concat(
    path("hello" / Segment) { name => renderHelloPage(name).toUtf8Http },
  )
