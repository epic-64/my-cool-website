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
  val contract = ContractBusiness.initialize("ABC123", "Alice", "Bob")
  val saved = contract.save()

  if saved.isFailure then
    println(s"Failed to save contract: ${saved.failed.get.getMessage}")
    return

  val signed1 = contract.signByParty1()
  if signed1.isFailure then
    println(s"Failed to sign by Party1: ${signed1.failed.get.getMessage}")
    return

  val signed2 = signed1.get.signByParty2()
  if signed2.isFailure then
    println(s"Failed to sign by Party2: ${signed2.failed.get.getMessage}")
    return

  val email1 = signed2.get.sendEmail(signed2.get.party1, "Contract signed!")
  if email1.isFailure then
    println(s"Failed to send email to Party1: ${email1.failed.get.getMessage}")
    return

  

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
    c <- Success(Contract("ABC123", "Alice", "Bob").toStatefulContract)
    c <- errContext("Failed to save contract")(c.save())
    c <- errContext("Failed to sign by Party1")(c.signByParty1())
    c <- errContext("Failed to sign by Party2")(c.signByParty2())
    _ <- errContext("Failed to send email to Party1")(c.sendEmail(c.party1, "Contract signed!"))
    _ <- errContext("Failed to send email to Party2")(c.sendEmail(c.party2, "Contract signed!"))
  yield c) match
    case Failure(e) => println(s"Operation failed: ${e.getMessage}")
    case Success(c) => println("Contract process completed successfully.")