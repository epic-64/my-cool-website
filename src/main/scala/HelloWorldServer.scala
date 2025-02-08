import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.server.Directives.*
import spray.json.DefaultJsonProtocol.* // Provides JSON formatting

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

// ✅ Define a case class for the JSON response
case class SumResult(x: Int, y: Int, sum: Int)

// ✅ Create a JSON formatter (needed for automatic conversion)
given spray.json.RootJsonFormat[SumResult] = jsonFormat3(SumResult.apply)

object HelloWorldServer {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem                        = ActorSystem("my-actor-system")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val routes = concat(
      path("hello")(complete("Hello, world!")),
      path("goodbye")(complete("Goodbye, world!")),
      path("ping")(complete("pong")),
      path("hello" / Segment)(name => complete(s"Hello, $name!")),

      // ✅ JSON response for /add/{x}/{y}
      path("add" / IntNumber / IntNumber) { (x, y) =>
        complete(SumResult(x, y, x + y)) // Automatically converts to JSON
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
