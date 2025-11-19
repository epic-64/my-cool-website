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

  def signByParty1(stateful: StatefulContract): Try[StatefulContract] =
    if stateful.state != ContractState.Unsigned then
      Failure(new Exception("Invalid state: Party1 cannot sign now."))
    else
      val contract = Contract[ContractState.Unsigned.type](stateful.id, stateful.party1, stateful.party2)
      val next = contract.signByParty1
      val updated = StatefulContract.fromContract(
        contract = next,
        party1Sig = SignatureStatus(true, Some(Instant.now)),
        party2Sig = stateful.party2Signature,
        state = ContractState.Party1Signed
      )
      sendEmail(stateful.party1, stateful.id, "Party1 signed the contract.")
      ContractPersistence.save(updated).map(_ => updated)

  def signByParty2(stateful: StatefulContract): Try[StatefulContract] =
    if stateful.state != ContractState.Party1Signed then Failure(new Exception("Invalid state: Party2 cannot sign now."))
    else
      val contract = Contract[ContractState.Party1Signed.type](stateful.id, stateful.party1, stateful.party2)
      val next = contract.signByParty2
      val updated = StatefulContract.fromContract(next,
        stateful.party1Signature, SignatureStatus(true, Some(Instant.now)), ContractState.FullySigned)
      sendEmail(stateful.party2, stateful.id, "Party2 signed the contract.")
      ContractPersistence.save(updated).map(_ => updated)

  def sendEmail(recipient: String, contractId: String, message: String): Try[Unit] = Try:
    println(s"Sending email to $recipient: $message")

@main def main(): Unit =
  Contract("ABC123", "Alice", "Bob").toStatefulContract.save() match
    case Failure(e) => println(s"Failed to save contract: ${e.getMessage}");
    case Success(c) => ContractBusiness.signByParty1(c) match
      case Failure(e) => println(s"Failed to sign by Party1: ${e.getMessage}");
      case Success(c) => ContractBusiness.signByParty2(c) match
        case Failure(e) => println(s"Failed to sign by Party2: ${e.getMessage}");
        case Success(c) => ContractBusiness.sendEmail(c.party1, c.id, "Contract signed!") match
          case Failure(e) => println(s"Failed to send email: ${e.getMessage}");
          case Success(_) => ContractBusiness.sendEmail(c.party2, c.id, "Contract signed!") match
            case Failure(e) => println(s"Failed to send email: ${e.getMessage}");
            case Success(_) => println("Contract process completed successfully.")