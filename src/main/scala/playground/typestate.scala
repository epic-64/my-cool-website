package playground

import ContractModelRW.*
import upickle.default.{read, write}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success, Try}

object ContractPersistence:
  val filePath = "contract_state.json"

  def save(contract: StatefulContract[? <: ContractState]): Try[Unit] = Try:
    val dto = StatefulContractDto(contract.id, contract.party1, contract.party2, contract.party1Signature, contract.party2Signature, contract.state)
    val json = write(dto, indent = 2)
    Files.write(Paths.get(filePath), json.getBytes(StandardCharsets.UTF_8))

  def load(): Try[StatefulContractDto] = Try:
    val jsonStr = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
    read[StatefulContractDto](jsonStr)

def example1(): Unit =
  val contract = Contract("ABC123", "Alice", "Bob").toStatefulContract

  val saved = try {
    contract.save().get
  } catch {
    case e: Exception =>
      println(s"Failed to save contract: ${e.getMessage}")
      return
  }

  val signed1 = try {
    contract.signByParty1().get
  } catch {
    case e: Exception =>
      println(s"Failed to sign by Party1: ${e.getMessage}")
      return
  }

  val signed2 = try {
    signed1.signByParty2().get
  } catch {
    case e: Exception =>
      println(s"Failed to sign by Party2: ${e.getMessage}")
      return
  }

  try {
    signed2.sendEmail(signed2.party1, "Contract signed!").get
  } catch {
    case e: Exception =>
      println(s"Failed to send email to Party1: ${e.getMessage}")
      return
  }

  try {
    signed2.sendEmail(signed2.party2, "Contract signed!").get
  } catch {
    case e: Exception =>
      println(s"Failed to send email to Party2: ${e.getMessage}")
      return
  }

  println("Contract process completed successfully.")

def example2(): Unit =
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

def recover[A](operation: String)(block: => Try[A]): Try[A] =
  block.recoverWith:
    case e => Failure(new Exception(s"$operation: ${e.getMessage}", e))

def example3(): Unit =
  (for
    c <- Success(Contract("ABC123", "Alice", "Bob").toStatefulContract)
    c <- recover("Failed to save contract")(c.save())
    c <- recover("Failed to sign by Party1")(c.signByParty1())
    c <- recover("Failed to sign by Party2")(c.signByParty2())
    _ <- recover("Failed to send email to Party1")(c.sendEmail(c.party1, "Contract signed!"))
    _ <- recover("Failed to send email to Party2")(c.sendEmail(c.party2, "Contract signed!"))
  yield c) match
    case Failure(e) => println(s"Operation failed: ${e.getMessage}")
    case Success(c) => println("Contract process completed successfully.")

def example4(): Unit =
  (for
    c <- recover("Failed to load contract") { ContractPersistence.load() }
    c <- recover("Contract is not in unsigned State") { c.asUnsigned() }
    c <- recover("Failed to sign by Party1") { c.signByParty1() }
    c <- recover("Failed to sign by Party2") { c.signByParty2() }
    _ <- recover("Failed to send email to Party1") { c.sendEmail(c.party1, "Contract signed!") }
    _ <- recover("Failed to send email to Party2") { c.sendEmail(c.party2, "Contract signed!") }
  yield (c)) match
    case Failure(e) => println(s"Operation failed: ${e.getMessage}")
    case Success(c) => println("Contract process completed successfully.")

@main def main(): Unit =
  example1()
  example2()
  example3()
  example4()