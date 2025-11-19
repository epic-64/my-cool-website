package playground

import models.ContractModelRW.*
import models.{SignatureStatus, StatefulContract, StatefulContractDto}
import upickle.default.{read, write}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success, Try}

object ContractPersistence {
  val filePath = "contract_state.json"

  def save(contract: StatefulContract[? <: ContractState]): Try[Unit] = Try:
    val dto = StatefulContractDto(contract.id, contract.party1, contract.party2, contract.party1Signature, contract.party2Signature, contract.state)
    val json = write(dto, indent = 2)
    Files.write(Paths.get(filePath), json.getBytes(StandardCharsets.UTF_8))

  def load(): Try[StatefulContract[? <: ContractState]] = Try:
    val jsonStr = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
    val dto = read[StatefulContractDto](jsonStr)
    dto.toTyped
}

object ContractBusiness:
  def initialize(id: String, party1: String, party2: String): StatefulContract[ContractState.Unsigned.type] =
    val unsigned = SignatureStatus(signed = false, timestamp = None)
    StatefulContract(id, party1, party2, unsigned, unsigned, ContractState.Unsigned)

@main def main(): Unit =
  val contract = ContractBusiness.initialize("ABC123", "Alice", "Bob")

  val saved = try {
    contract.save().get
  } catch { case e: Exception =>
      println(s"Failed to save contract: ${e.getMessage}")
      return
  }

  val signed1 = try {
    contract.signByParty1().get
  } catch { case e: Exception =>
      println(s"Failed to sign by Party1: ${e.getMessage}")
      return
  }

  val signed2 = try {
    signed1.signByParty2().get
  } catch { case e: Exception =>
      println(s"Failed to sign by Party2: ${e.getMessage}")
      return
  }

  try {
    signed2.sendEmail(signed2.party1, "Contract signed!").get
  } catch { case e: Exception =>
      println(s"Failed to send email to Party1: ${e.getMessage}")
      return
  }

  try {
    signed2.sendEmail(signed2.party2, "Contract signed!").get
  } catch { case e: Exception =>
      println(s"Failed to send email to Party2: ${e.getMessage}")
      return
  }

  println("Contract process completed successfully.")

  Contract("ABC123", "Alice", "Bob").toStatefulContract.save() match
    case Failure(err) => println(s"Failed to save contract: ${err.getMessage}");
    case Success(c) => c.signByParty1() match
      case Failure(err) => println(s"Failed to sign by Party1: ${err.getMessage}");
      case Success(c) => c.signByParty2() match
        case Failure(err) => println(s"Failed to sign by Party2: ${err.getMessage}");
        case Success(c) => c.sendEmail(c.party1, "Contract signed!") match
          case Failure(err) => println(s"Failed to send email: ${err.getMessage}");
          case Success(_) => c.sendEmail(c.party2, "Contract signed!") match
            case Failure(err) => println(s"Failed to send email: ${err.getMessage}");
            case Success(_) => println("Contract process completed successfully.")

  def errContext[A](operation: String)(block: => Try[A]): Try[A] =
    block.recoverWith:
      case e => Failure(new Exception(s"$operation: ${e.getMessage}", e))

  (for
    c <- errContext("Failed to initialize contract")(Success(ContractBusiness.initialize("ABC123", "Alice", "Bob")))
    c <- errContext("Failed to save contract")(c.save())
    c <- errContext("Failed to sign by Party1")(c.signByParty1())
    c <- errContext("Failed to sign by Party2")(c.signByParty2())
    _ <- errContext("Failed to send email to Party1")(c.sendEmail(c.party1, "Contract signed!"))
    _ <- errContext("Failed to send email to Party2")(c.sendEmail(c.party2, "Contract signed!"))
  yield c) match
    case Failure(e) => println(s"Operation failed: ${e.getMessage}")
    case Success(c) => println("Contract process completed successfully.")