import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import spray.json.DefaultJsonProtocol.*
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.directives.FileAndResourceDirectives.getFromResourceDirectory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

// ✅ Define a case class for the response
case class SumResult(x: Int, y: Int, sum: Int)

// ✅ Add an implicit JSON format
given spray.json.RootJsonFormat[SumResult] = jsonFormat3(SumResult.apply)

// ✅ Define case classes for JSON input and response
case class MultiplyRequest(x: Int, y: Int)
case class MultiplyResult(x: Int, y: Int, product: Int)

// ✅ JSON formatters (required for automatic JSON handling)
given spray.json.RootJsonFormat[MultiplyRequest] = jsonFormat2(MultiplyRequest.apply)
given spray.json.RootJsonFormat[MultiplyResult]  = jsonFormat3(MultiplyResult.apply)

object HelloWorldServer {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem                        = ActorSystem("my-actor-system")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val routes = concat(
      path("hello")(complete("Hello, world!")),
      path("goodbye")(complete("Goodbye, world!")),
      path("ping")(complete("pong")),
      path("hello" / Segment)(name => complete(s"Hello, $name!")),
      path("add" / IntNumber / IntNumber) { (x, y) =>
        complete(SumResult(x, y, x + y)) // Now it works!
      },

      // ✅ NEW: POST /multiply endpoint that receives JSON input
      path("multiply") {
        post {
          entity(as[MultiplyRequest]) { request =>
            val result = MultiplyResult(request.x, request.y, request.x * request.y)
            complete(result) // Automatically converted to JSON
          }
        }
      },

      pathPrefix("assets") {
        respondWithHeader(RawHeader("Cache-Control", "public, max-age=86400")) {
          getFromResourceDirectory("public/assets") // Serve files
        }
      }
    )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

    println("Server running at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // Wait for user to press Enter

    bindingFuture
      .flatMap(_.unbind())                 // Unbind the port
      .onComplete(_ => system.terminate()) // Shut down actor system
  }
}
