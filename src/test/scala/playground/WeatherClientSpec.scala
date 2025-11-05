package playground

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.{Failure, Success, Try}

class WeatherClientSpec extends AnyWordSpec with Matchers {
  "WeatherClient" should {
    "return formatted weather data when fetch succeeds and JSON parses" in {
      given WeatherHttp with
        def fetch(latitude: Double, longitude: Double): Try[String] =
          Success("""{"current_weather":{"temperature":1.0,"windspeed":5.0,"winddirection":90.0}}""")

      val logger = MemoryLogger();
      given Logger = logger

      val result = WeatherClient().get(52.52, 13.41)

      result shouldBe List(
        "Current temperature: 1.0 C",
        "Wind speed: 5.0 km/h",
        "Wind direction: 90.0 degrees"
      )
      logger.errorMessages shouldBe Nil
    }

    "return an error message when fetch fails" in {
      given WeatherHttp with
        def fetch(latitude: Double, longitude: Double): Try[String] = Failure(new RuntimeException("Oops"))

      val logger = MemoryLogger();
      given Logger = logger

      val result = WeatherClient().get(0, 0)

      result shouldBe List("We had an error fetching the weather data. Please try again later.")
      logger.errorMessages shouldBe List("RuntimeException: Oops")
    }

    "return a parse error message when JSON cannot be parsed" in {
      given WeatherHttp with
        def fetch(latitude: Double, longitude: Double): Try[String] = Success("{" )

      val logger = MemoryLogger();
      given Logger = logger

      val result = WeatherClient().get(0, 0)

      result shouldBe List("We had an error parsing the response. Please try again later.")
      logger.errorMessages should have size 1
      logger.errorMessages.head.startsWith("Parse failure:") shouldBe true
    }

    "round numeric values to one decimal place in formatting" in {
      given WeatherHttp with
        def fetch(latitude: Double, longitude: Double): Try[String] =
          Success("""{"current_weather":{"temperature":1.24,"windspeed":7.25,"winddirection":270.0}}""")

      val logger = MemoryLogger();
      given Logger = logger

      val result = WeatherClient().get(1.234, 5.678)

      result shouldBe List(
        "Current temperature: 1.2 C",  // 1.24 -> 1.2 (round down)
        "Wind speed: 7.3 km/h",        // 7.25 -> 7.3 (round half up)
        "Wind direction: 270.0 degrees"
      )
      logger.errorMessages shouldBe Nil
    }

    "log error message when response does not contain temperature" in {
      given WeatherHttp with
        def fetch(latitude: Double, longitude: Double): Try[String] =
          Success("""{"current_weather":{"temp":1.0,"windspeed":5.0,"winddirection":90.0}}""")

      val logger = MemoryLogger();
      given Logger = logger

      val result = WeatherClient().get(0, 0)

      result shouldBe List("We had an error parsing the response. Please try again later.")
      logger.errorMessages should have size 1
      logger.errorMessages.head.startsWith("Parse failure:") shouldBe true
    }
  }
}
