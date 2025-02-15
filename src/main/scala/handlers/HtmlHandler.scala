package handlers

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

object HtmlHandler:
  def routes: Route = concat(
    pathSingleSlash {
      getFromResource("public/index.html")
    },
    path("hello")(complete("Hello, world!")),
    path("goodbye")(complete("Goodbye, world!")),
    path("ping")(complete("pong")),
    path("hello" / Segment) { name =>
      complete(s"Hello, $name!")
    },
    path("404") {
      complete("<html><h1>404 - Not Found</h1></html>")
    }
  )

