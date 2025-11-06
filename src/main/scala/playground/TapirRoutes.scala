package playground

import sttp.tapir._
import sttp.tapir.server.pekkohttp.PekkoHttpServerInterpreter
import org.apache.pekko.http.scaladsl.server.Route
import scala.concurrent.{Future, ExecutionContext}
import upickle.default.{ReadWriter, macroRW}
import sttp.tapir.json.upickle._
import sttp.tapir.generic.auto._

object TapirRoutes {
  // Simple endpoint: GET /weather returns a string
  val weatherEndpoint: PublicEndpoint[Unit, Unit, String, Any] =
    endpoint.get.in("weather").out(stringBody)

  // Requires an implicit/given ExecutionContext (provided by ActorSystem in HelloWorldServer)
  def weatherRoute(using ec: ExecutionContext): Route =
    PekkoHttpServerInterpreter().toRoute(weatherEndpoint.serverLogicSuccess { _ =>
      Future.successful("The weather is sunny!")
    })

  final case class WeatherResponse(city: String, description: String, tempC: Double)
  object WeatherResponse { implicit val rw: ReadWriter[WeatherResponse] = macroRW }

  // Endpoint: GET /weather/{city}?units=metric|imperial returns JSON weather payload
  val weatherByCityEndpoint: PublicEndpoint[(String, Option[String]), Unit, WeatherResponse, Any] =
    endpoint.get
      .in("weather" / path[String]("city"))
      .in(query[Option[String]]("units"))
      .out(jsonBody[WeatherResponse])

  def weatherByCityRoute(using ec: ExecutionContext): Route =
    PekkoHttpServerInterpreter().toRoute(weatherByCityEndpoint.serverLogicSuccess { case (city, unitsOpt) =>
      // Simple fake temperature conversion for demo
      val tempC = if unitsOpt.contains("imperial") then 75.0 else 24.0
      Future.successful(WeatherResponse(city, s"Sunny in $city", tempC))
    })

  // Aggregate Tapir routes here to expose multiple endpoints easily
  def allTapirRoutes(using ec: ExecutionContext): Route =
    import org.apache.pekko.http.scaladsl.server.Directives.concat
    concat(
      weatherRoute,
      weatherByCityRoute,
    )
}
