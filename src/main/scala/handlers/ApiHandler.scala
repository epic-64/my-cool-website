package handlers

import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import models.*
import models.MathModels.given
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import spray.json.*

object ApiHandler:
  def routes: Route = concat(
    path("add" / IntNumber / IntNumber) { (x, y) =>
      complete(SumResult(x, y, x + y))
    },
    path("multiply") {
      post {
        entity(as[MultiplyRequest]) { request =>
          val result = MultiplyResult(request.x, request.y, request.x * request.y)
          complete(result)
        }
      }
    }
  )
