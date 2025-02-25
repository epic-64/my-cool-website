package handlers

import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import models.*
import models.MathModels.given
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity}
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import spray.json.enrichAny

object ApiHandler:
  def routes: Route = concat(
    path("add-stream" / IntNumber / IntNumber) { (x, y) =>
      val result         = SumResult(x, y, x + y).toJson.compactPrint
      val responseStream = Source.single(ByteString(result))
      complete(HttpEntity(ContentTypes.`application/json`, responseStream))
    },
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
