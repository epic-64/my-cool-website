package playground

import playground.ContractModelRW.*
import upickle.default.{read, write}

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success, Try}

extension (file: File)
  def writeText(text: String): Try[Unit] = Try:
    Files.writeString(file.toPath, text, StandardCharsets.UTF_8)

  def readText(): Try[String] = Try:
    Files.readString(file.toPath, StandardCharsets.UTF_8)

object ContractPersistence:
  def filePath(id: String) = s"data/contract_state_$id.json"

  def save(contract: StatefulContract[? <: ContractState]): Try[Unit] = Try:
    StatefulContractDto.fromStatefulContract(contract).toJson.tap: json =>
      File(filePath(contract.id)).tap: file =>
        Option(file.getParentFile).foreach(_.mkdirs())
        file.writeText(json)

  def load(id: String): Try[StatefulContractDto] = Try:
    val json = File(filePath(id)).readText().get
    read[StatefulContractDto](json)

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

def context[A](info: String)(block: => Try[A]): Try[A] =
  block.recoverWith:
    case e => Failure(new Exception(s"$info: ${e.getMessage}", e))

def example22(): Unit =
  Success(Contract("ABC123", "Alice", "Bob").toStatefulContract)
    .flatMap { c => context("Failed to save contract")(c.save()) }
    .flatMap { c => context("Failed to sign by Party1")(c.signByParty1()) }
    .flatMap { c => context("Failed to sign by Party2")(c.signByParty2()) }
    .flatMap { c => context("Failed to send email to Party1")(c.sendEmail(c.party1, "Contract signed!")).map(_ => c) }
    .flatMap { c => context("Failed to send email to Party2")(c.sendEmail(c.party2, "Contract signed!")).map(_ => c) }
  match
    case Failure(e) => println(s"Operation failed: ${e.getMessage}")
    case Success(c) => println("Contract process completed successfully.")

def example3(): Unit =
  (for
    c <- Success(Contract("ABC123", "Alice", "Bob").toStatefulContract)
    c <- context("Failed to save contract") { c.save() }
    c <- context("Failed to sign by Party1") { c.signByParty1() }
    c <- context("Failed to sign by Party2") { c.signByParty2() }
    _ <- context("Failed to send email to Party1") { c.sendEmail(c.party1, "Contract signed!") }
    _ <- context("Failed to send email to Party2") { c.sendEmail(c.party2, "Contract signed!") }
  yield c) match
    case Failure(e) => println(s"Operation failed: ${e.getMessage}")
    case Success(c) => println("Contract process completed successfully.")

def example4(): Unit =
  (for
    c <- context("Failed to load contract") { ContractPersistence.load("ABC123") }
    c <- context("Contract is not in unsigned State") { c.asUnsigned() }
    c <- context("Failed to sign by Party1") { c.signByParty1() }
    c <- context("Failed to sign by Party2") { c.signByParty2() }
    _ <- context("Failed to send email to Party1") { c.sendEmail(c.party1, "Contract signed!") }
    _ <- context("Failed to send email to Party2") { c.sendEmail(c.party2, "Contract signed!") }
  yield (c)) match
    case Failure(e) => println(s"Operation failed: ${e.getMessage}")
    case Success(c) => println("Contract process completed successfully.")

@main def main(): Unit =
  example1()
  example2()
  example22()
  example3()
  example4()