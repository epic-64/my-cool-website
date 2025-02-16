package handlers

import org.apache.pekko.http.scaladsl.model.headers.RawHeader
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route

object StaticHandler:
  def routes: Route = concat(
    path("assets-test") {
      getFromResource("public/assets/css/style.css")
    },
    pathPrefix("assets") {
      respondWithHeader(RawHeader("Cache-Control", "public, max-age=86400")) {
        getFromResourceDirectory("public/assets")
      }
    },
  )
