import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object HelloWorldServer:
  def main(args: Array[String]): Unit =
    implicit val system: ActorSystem                        = ActorSystem("my-actor-system")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(Routes.allRoutes)
    println("Server running at http://localhost:8080/")

    Await.result(bindingFuture.flatMap(_.whenTerminated), Duration.Inf)