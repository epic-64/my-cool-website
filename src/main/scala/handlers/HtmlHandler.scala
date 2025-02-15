package handlers

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import views.html.hello

object HtmlHandler:
  def routes: Route = concat(
    pathSingleSlash {
      getFromResource("public/index.html")
    },
    path("hello")(complete("Hello, world!")),
    path("goodbye")(complete("Goodbye, world!")),
    path("ping")(complete("pong")),
    path("hello" / Segment) { name =>
      val renderedHtml = hello(name).body // ✅ Call the Twirl template
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, renderedHtml))
    },
    path("404") {
      complete("<html><h1>404 - Not Found</h1></html>")
    }
  )
