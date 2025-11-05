package playground

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class UserServiceSpec extends AnyWordSpec with Matchers:
  "UserService" should {
    "uppercase user names from a test client" in {
      val testInput = List("alpha", "Beta", "GAMMA")
      // Provide a test-local UserClient root dependency.
      given testUserClient: UserClient = new UserClient:
        override def get(): List[String] = testInput
      // Import the reusable downstream givens (repository, service).
      import playground.given
      val service = summon[UserService]
      val result  = service.list()
      // Hard-coded expected output (not derived from testInput to avoid duplicating business logic).
      val expected = List("ALPHA", "BETA", "GAMMA")
      result shouldEqual expected
    }
  }
