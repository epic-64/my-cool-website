package playground

import upickle.default.*
import upickle.implicits.key

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import scala.util.{Failure, Success, Try}
import scala.util.chaining.scalaUtilChainingOps

// Domain models with friendly field names mapped to Open-Meteo JSON keys
case class CurrentWeather(
    @key("temperature")   temperatureC: Double,
    @key("windspeed")     windSpeedKmh: Double,
    @key("winddirection") windDirectionDeg: Double,
)
case class WeatherResponse(
    @key("current_weather") currentWeather: CurrentWeather
)

object WeatherResponse:
  given ReadWriter[CurrentWeather] = macroRW
  given ReadWriter[WeatherResponse] = macroRW

trait WeatherHttp:
  def fetch(latitude: Double, longitude: Double): Try[String]

class LiveWeatherHttp(using httpClient: HttpClient) extends WeatherHttp:
  private def endpoint(latitude: Double, longitude: Double): URI =
    val baseUrl = "https://api.open-meteo.com/v1/forecast"
    URI.create(s"$baseUrl?latitude=$latitude&longitude=$longitude&current_weather=true")

  def fetch(latitude: Double, longitude: Double): Try[String] =
    val request = HttpRequest.newBuilder(endpoint(latitude, longitude)).GET().build()
    Try(httpClient.send(request, HttpResponse.BodyHandlers.ofString())).map(_.body())

class WeatherClient(using http: WeatherHttp):
  def get(latitude: Double, longitude: Double): List[String] =
    http.fetch(latitude, longitude) match
      case Failure(e) => List(s"ERROR: ${e.getClass.getSimpleName}: ${e.getMessage}")
      case Success(body) => Try(read[WeatherResponse](body)) match
        case Failure(parseErr) => List(s"ERROR: Parse failure: ${parseErr.getMessage}")
        case Success(model) => model.currentWeather pipe { weather => List(
            f"Current temperature: ${weather.temperatureC}%.1f C",
            f"Wind speed: ${weather.windSpeedKmh}%.1f km/h",
            s"Wind direction: ${weather.windDirectionDeg} degrees"
          ) }

object ProdWiring:
  given HttpClient = HttpClient.newHttpClient()
  given WeatherHttp = LiveWeatherHttp()
  given WeatherClient = WeatherClient()

def runApp()(using client: WeatherClient): Unit =
  println("=== Live Weather ===")
  client.get(52.52, 13.41).foreach(println)

def runTest(): Unit =
  val stubJson = """{"current_weather":{"temperature":1.0,"windspeed":5.0,"winddirection":90.0}}"""
  given WeatherHttp = (latitude: Double, longitude: Double) => Success(stubJson)
  given WeatherClient = WeatherClient()

  println("=== Test Weather Data (Stubbed WeatherHttp) ===")
  val result = summon[WeatherClient].get(0, 0)
  result.foreach(println)

  val expected = List(
    "Current temperature: 1.0 C",
    "Wind speed: 5.0 km/h",
    "Wind direction: 90.0 degrees"
  )
  assert(result == expected, s"Expected $expected but got $result")

@main def main(): Unit =
  import ProdWiring.given
  runApp()
  runTest()
