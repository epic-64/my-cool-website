import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object HelloWorldServer:
  def main(args: Array[String]): Unit =
    implicit val system: ActorSystem                        = ActorSystem("my-actor-system")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(Routes.allRoutes)

    println("Server running at http://localhost:8080/")
    println("Press RETURN to stop...")
    StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
