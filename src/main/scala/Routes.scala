import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import handlers.{ApiHandler, HtmlHandler, StaticHandler}
import scala.concurrent.ExecutionContext
import playground.TapirRoutes

object Routes:
  def allRoutes(using ec: ExecutionContext): Route =
    concat(
      StaticHandler.routes,
      HtmlHandler.routes,
      ApiHandler.routes,
      TapirRoutes.weatherRoute, // Tapir weather endpoint
    )
