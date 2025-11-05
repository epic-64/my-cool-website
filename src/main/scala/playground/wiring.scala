package playground

import upickle.default.*
import upickle.implicits.key

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success, Try}

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

class WeatherClient(httpClient: HttpClient = HttpClient.newHttpClient()):
  private def endpoint(latitude: Double, longitude: Double): URI =
    val baseUrl = "https://api.open-meteo.com/v1/forecast"
    URI.create(s"$baseUrl?latitude=$latitude&longitude=$longitude&current_weather=true")

  def get(latitude: Double, longitude: Double): List[String] =
    val request = HttpRequest.newBuilder(endpoint(latitude, longitude)).GET().build()

    Try(httpClient.send(request, HttpResponse.BodyHandlers.ofString())) match
      case Failure(e) => List(s"ERROR: ${e.getClass.getSimpleName}: ${e.getMessage}")
      case Success(resp) if resp.statusCode() / 100 != 2 => List(s"ERROR: HTTP ${resp.statusCode()}")
      case Success(resp) => Try(read[WeatherResponse](resp.body())) match
        case Failure(parseErr) => List(s"ERROR: Parse failure: ${parseErr.getMessage}")
        case Success(model) => model.currentWeather pipe { weather => List(
            f"Current temperature: ${weather.temperatureC}%.1f C",
            f"Wind speed: ${weather.windSpeedKmh}%.1f km/h",
            s"Wind direction: ${weather.windDirectionDeg} degrees"
          ) }

object ProdWiring:
  given WeatherClient = WeatherClient()

def runApp()(using client: WeatherClient): Unit =
  println("=== Live Weather ===")
  client.get(52.52, 13.41).foreach(println)

def runTest(): Unit =
  // Stub client returning deterministic data for test scenario
  val testData = List(
    "Current temperature: 1.0 C",
    "Wind speed: 5.0 km/h",
    "Wind direction: 90 degrees"
  )
  given WeatherClient = new WeatherClient:
    override def get(latitude: Double, longitude: Double): List[String] = testData

  println("=== Test Weather Data ===")
  val result = summon[WeatherClient].get(0, 0).map(_.toUpperCase)
  result.foreach(println)

  val expected = List(
    "CURRENT TEMPERATURE: 1.0 C",
    "WIND SPEED: 5.0 KM/H",
    "WIND DIRECTION: 90 DEGREES"
  )
  assert(result == expected, s"Expected $expected but got $result")

@main def main(): Unit =
  import ProdWiring.given
  runApp()
  runTest()
