import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import handlers.{ApiHandler, HtmlHandler, StaticHandler}

object Routes:
  def allRoutes: Route =
    concat(
      StaticHandler.routes,
      HtmlHandler.routes,
      ApiHandler.routes,
    )
