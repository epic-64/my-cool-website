package playground

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.{Failure, Success, Try}

class WeatherClientSpec extends AnyWordSpec with Matchers {

  private def withStub(stub: Try[String])(body: (WeatherClient, MemoryLogger) => Any): Unit = {
    given WeatherHttp with
      def fetch(latitude: Double, longitude: Double): Try[String] = stub
    given logger: MemoryLogger = new MemoryLogger()
    body(WeatherClient(), logger)
  }

  "WeatherClient" should {
    "return formatted weather data when fetch succeeds and JSON parses" in withStub(
      Success("""{"current_weather":{"temperature":1.0,"windspeed":5.0,"winddirection":90.0}}""")
    ) { (client, logger) =>
      client.get(0, 0) shouldBe List(
        "Current temperature: 1.0 C",
        "Wind speed: 5.0 km/h",
        "Wind direction: 90.0 degrees"
      )
      logger.errorMessages shouldBe Nil
    }

    "return an error message when fetch fails" in withStub(
      Failure(new RuntimeException("Oops"))
    ) { (client, logger) => {
      client.get(0, 0) shouldBe List("We had an error fetching the weather data. Please try again later.")
      logger.errorMessages shouldBe List("RuntimeException: Oops")
    }}

    "return a parse error message when JSON cannot be parsed" in withStub(
      Success("{")
    ) { (client, logger) =>
      client.get(0, 0) shouldBe List("We had an error parsing the response. Please try again later.")
      logger.errorMessages should have size 1
      logger.errorMessages.head.startsWith("Parse failure:") shouldBe true
    }

    "round numeric values to one decimal place in formatting" in withStub(Success(
      """{"current_weather":{"temperature":1.24,"windspeed":7.25,"winddirection":270.0}}"""
    )) { (client, logger) =>
      client.get(1.234, 5.678) shouldBe List(
        "Current temperature: 1.2 C",
        "Wind speed: 7.3 km/h",
        "Wind direction: 270.0 degrees"
      )
      logger.errorMessages shouldBe Nil
    }

    "log error message when response does not contain temperature" in withStub(Success(
      """{"current_weather":{"temp":1.0,"windspeed":5.0,"winddirection":90.0}}"""
    )) { (client, logger) =>
      client.get(0, 0) shouldBe List("We had an error parsing the response. Please try again later.")
      logger.errorMessages should have size 1

      val msg = logger.errorMessages.head.toLowerCase
      msg.startsWith("parse failure:") shouldBe true
      msg.contains("temperature") shouldBe true
    }

    "ignore unexpected fields in response" in withStub(Success(
      """{"current_weather":{"temperature":1.0,"windspeed":5.0,"winddirection":90.0,"extra":1}}"""
    )) { (client, logger) =>
      client.get(0, 0) shouldBe List(
        "Current temperature: 1.0 C",
        "Wind speed: 5.0 km/h",
        "Wind direction: 90.0 degrees"
      )
      logger.errorMessages shouldBe Nil
    }
  }
}
