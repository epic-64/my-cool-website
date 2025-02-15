package handlers

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

object StaticHandler:
  def routes: Route =
    pathPrefix("assets") {
      respondWithHeader(RawHeader("Cache-Control", "public, max-age=86400")) {
        getFromResourceDirectory("public/assets")
      }
    }
