package handlers

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import models.*
import models.MathModels.given
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
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
