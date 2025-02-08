import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object HelloWorldServer {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem                        = ActorSystem("my-actor-system")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    // Define multiple routes
    val routes = concat(
      path("hello")(complete("Hello, world!")),
      path("goodbye")(complete("Goodbye, world!")),
      path("ping")(complete("pong")),
      path("hello" / Segment)(name => complete(s"Hello, $name!"))
    )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

    println("Server running at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // Wait for user to press Enter

    bindingFuture
      .flatMap(_.unbind())                 // Unbind the port
      .onComplete(_ => system.terminate()) // Shut down actor system
  }
}
