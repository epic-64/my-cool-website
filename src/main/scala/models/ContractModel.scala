package models

import java.time.Instant
import upickle.default.{ReadWriter, macroRW}
import playground.{Contract, ContractState}

import scala.util.{Failure, Try}

case class SignatureStatus(signed: Boolean, timestamp: Option[Instant])

case class StatefulContract(
  id: String,
  party1: String,
  party2: String,
  party1Signature: SignatureStatus,
  party2Signature: SignatureStatus,
  state: ContractState
):
  def save(): Try[StatefulContract] = playground.ContractPersistence.save(this).map(_ => this)

  def load(): Try[StatefulContract] = playground.ContractPersistence.load()

  def sendEmail(recipient: String, message: String): Try[Unit] = Try:
    throw new Exception("Not implemented")
    println(s"Sending email to $recipient: $message")

  def signByParty1(): Try[StatefulContract] =
    if state != ContractState.Unsigned then
      Failure(new Exception("Invalid state: Party1 cannot sign now."))
    else
      val contract = Contract[ContractState.Unsigned.type](id, party1, party2)
      val next = contract.signByParty1
      val updated = StatefulContract.fromContract(
        contract = next,
        party1Sig = SignatureStatus(true, Some(Instant.now)),
        party2Sig = party2Signature,
        state = ContractState.Party1Signed
      )
      sendEmail(party1, "Party1 signed the contract.")
      updated.save().map(_ => updated)

  def signByParty2(): Try[StatefulContract] =
    if state != ContractState.Party1Signed then
      Failure(new Exception("Invalid state: Party2 cannot sign now."))
    else
      val contract = Contract[ContractState.Party1Signed.type](id, party1, party2)
      val next = contract.signByParty2
      val updated = StatefulContract.fromContract(
        contract = next,
        party1Sig = party1Signature,
        party2Sig = SignatureStatus(true, Some(Instant.now)),
        state = ContractState.FullySigned
      )
      sendEmail(party2, "Party2 signed the contract.")
      updated.save().map(_ => updated)



object StatefulContract:
  def fromContract[S <: ContractState](contract: Contract[S], party1Sig: SignatureStatus, party2Sig: SignatureStatus, state: ContractState): StatefulContract =
    StatefulContract(contract.id, contract.party1, contract.party2, party1Sig, party2Sig, state)

  def toContract(sc: StatefulContract): Option[Contract[? <: ContractState]] =
    sc.state match
      case ContractState.Unsigned => Some(Contract[ContractState.Unsigned.type](sc.id, sc.party1, sc.party2))
      case ContractState.Party1Signed => Some(Contract[ContractState.Party1Signed.type](sc.id, sc.party1, sc.party2))
      case ContractState.FullySigned => Some(Contract[ContractState.FullySigned.type](sc.id, sc.party1, sc.party2))
      case _ => None

object ContractModelRW {
  implicit val instantRW: ReadWriter[Instant] = upickle.default.readwriter[String].bimap[Instant](
    _.toString,
    Instant.parse
  )
  implicit val contractStateRW: ReadWriter[ContractState] = upickle.default.readwriter[String].bimap[ContractState](
    _.toString,
    s => playground.ContractState.valueOf(s)
  )
  implicit val signatureStatusRW: ReadWriter[SignatureStatus] = macroRW
  implicit val statefulContractRW: ReadWriter[StatefulContract] = macroRW
}
