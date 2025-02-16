import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object HelloWorldServer:
  def main(args: Array[String]): Unit =
    implicit val system: ActorSystem                        = ActorSystem("my-actor-system")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val port          = sys.env.getOrElse("PORT", "8080").toInt
    val bindingFuture = Http().newServerAt("0.0.0.0", port).bind(Routes.allRoutes)

    println("Server running at http://localhost:8080/")
    Await.result(bindingFuture.flatMap(_.whenTerminated), Duration.Inf)
