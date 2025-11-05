package playground

import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import scala.util.control.NonFatal
import upickle.default.*

// Domain models for (a subset of) the Open-Meteo response
case class CurrentWeather(temperature: Double, windspeed: Double, winddirection: Double)
case class WeatherResponse(current_weather: CurrentWeather)

object WeatherResponse:
  given ReadWriter[CurrentWeather] = macroRW
  given ReadWriter[WeatherResponse] = macroRW

class WeatherClient(
    latitude: Double = 52.52,
    longitude: Double = 13.41,
    httpClient: HttpClient = HttpClient.newHttpClient()
):
  private def endpoint: String =
    s"https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true"

  def get(): List[String] =
    val request = HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build()

    try
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
      if response.statusCode() / 100 != 2 then
        return List(s"ERROR: HTTP ${response.statusCode()}")

      val body = response.body()
      val parsed = read[WeatherResponse](body)
      val weather = parsed.current_weather

      List(
        f"Current temperature: ${weather.temperature}%.1f C",
        f"Wind speed: ${weather.windspeed}%.1f km/h",
        s"Wind direction: ${weather.winddirection} degrees"
      )
    catch
      case NonFatal(e) => List(s"ERROR: ${e.getClass.getSimpleName}: ${e.getMessage}")

class WeatherRepository(using client: WeatherClient):
  def findAll(): List[String] = client.get()

class WeatherService(using repo: WeatherRepository):
  def list(): List[String] = repo.findAll().map(_.toUpperCase)

given (using client: WeatherClient): WeatherRepository = WeatherRepository()
given (using repo: WeatherRepository): WeatherService = WeatherService()

object ProdWiring:
  given WeatherClient = WeatherClient()

def runApp()(using service: WeatherService): Unit =
  println("=== Live Weather (Uppercased) ===")
  summon[WeatherService].list().foreach(println)

def runTest(): Unit =
  val testInput = List("Temp: 1.0 C", "Wind: 5 km/h", "Direction: 90 deg")

  given WeatherClient = new WeatherClient:
    override def get(): List[String] = testInput

  val users = summon[WeatherService].list()
  println("=== Test Weather Data (Uppercased) ===")
  users.foreach(println)

  val expected = List("TEMP: 1.0 C", "WIND: 5 KM/H", "DIRECTION: 90 DEG")
  assert(users == expected, s"Expected $expected but got $users")

@main def main(): Unit =
  import ProdWiring.given // bring production WeatherClient into scope
  runApp()
  runTest() // execute test scenario
