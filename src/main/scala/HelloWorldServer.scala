import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object HelloWorldServer {

  def main(args: Array[String]): Unit = {
    // Create an Actor System
    implicit val system: ActorSystem = ActorSystem("my-actor-system")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    // Define route
    val route =
      path("hello") {
        get(complete("Hello, world!"))
      }

    // Start HTTP server on port 8080
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println("Server running at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // Wait for user to press Enter

    // Shutdown
    bindingFuture
      .flatMap(_.unbind()) // Unbind from the port
      .onComplete(_ => system.terminate()) // Shut down actor system
  }
}
