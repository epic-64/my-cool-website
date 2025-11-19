package playground

import models.ContractModelRW.*
import models.{SignatureStatus, StatefulContract}
import upickle.default.{read, write}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.time.Instant
import scala.util.{Failure, Success, Try}

object ContractPersistence {
  val filePath = "contract_state.json"

  def save(contract: StatefulContract): Try[Unit] = Try:
    val json = write(contract, indent = 2)
    Files.write(Paths.get(filePath), json.getBytes(StandardCharsets.UTF_8))

  def load(): Try[StatefulContract] = Try:
    val jsonStr = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
    read[StatefulContract](jsonStr)
}

object ContractBusiness:
  def initialize(id: String, party1: String, party2: String): StatefulContract =
    val unsigned = SignatureStatus(signed = false, timestamp = None)
    StatefulContract(id, party1, party2, unsigned, unsigned, ContractState.Unsigned)

@main def main(): Unit =
  Contract("ABC123", "Alice", "Bob").toStatefulContract.save() match
    case Failure(e) => println(s"Failed to save contract: ${e.getMessage}");
    case Success(c) => c.signByParty1() match
      case Failure(e) => println(s"Failed to sign by Party1: ${e.getMessage}");
      case Success(c) => c.signByParty2() match
        case Failure(e) => println(s"Failed to sign by Party2: ${e.getMessage}");
        case Success(c) => c.sendEmail(c.party1, "Contract signed!") match
          case Failure(e) => println(s"Failed to send email: ${e.getMessage}");
          case Success(_) => c.sendEmail(c.party2, "Contract signed!") match
            case Failure(e) => println(s"Failed to send email: ${e.getMessage}");
            case Success(_) => println("Contract process completed successfully.")