import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import handlers.{ApiHandler, HtmlHandler, StaticHandler}

object Routes:
  def allRoutes: Route =
    concat(
      StaticHandler.routes,
      HtmlHandler.routes,
      ApiHandler.routes,
    )
