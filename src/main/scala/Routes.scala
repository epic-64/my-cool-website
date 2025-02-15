import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import handlers.{ApiHandler, HtmlHandler, StaticHandler}

object Routes:
  def allRoutes: Route =
    concat(
      HtmlHandler.routes,
      ApiHandler.routes,
      StaticHandler.routes
    )
