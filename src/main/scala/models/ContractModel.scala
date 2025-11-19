package models

import java.time.Instant
import upickle.default.{ReadWriter, macroRW}
import playground.{Contract, ContractState}

import scala.util.Try

case class SignatureStatus(signed: Boolean, timestamp: Option[Instant])

case class StatefulContract[S <: ContractState](
  id: String,
  party1: String,
  party2: String,
  party1Signature: SignatureStatus,
  party2Signature: SignatureStatus,
  state: S
):
  def save(): Try[StatefulContract[S]] = playground.ContractPersistence.save(this).map(_ => this)

  def load(): Try[StatefulContract[? <: ContractState]] = playground.ContractPersistence.load()

  def sendEmail(recipient: String, message: String)(using ev: S =:= ContractState.FullySigned.type): Try[Unit] = Try:
    println(s"Sending email to $recipient: $message")

  def signByParty1()(using ev: S =:= ContractState.Unsigned.type): Try[StatefulContract[ContractState.Party1Signed.type]] =
    // Create a temporary contract just to call the method
    val tempContract = this.asInstanceOf[StatefulContract[ContractState.Unsigned.type]]
    val unsignedEvidence: ContractState.Unsigned.type =:= ContractState.Unsigned.type = 
      ev.asInstanceOf[ContractState.Unsigned.type =:= ContractState.Unsigned.type]
    val contract = Contract[ContractState.Unsigned.type](id, party1, party2)
    // Call with both () and using clause
    val next = contract.signByParty1()(using unsignedEvidence)
    val updated: StatefulContract[ContractState.Party1Signed.type] = StatefulContract.fromContract(
      contract = next,
      party1Sig = SignatureStatus(true, Some(Instant.now)),
      party2Sig = party2Signature,
      state = ContractState.Party1Signed
    )
    Try:
      updated.save().get

  def signByParty2()(using ev: S =:= ContractState.Party1Signed.type): Try[StatefulContract[ContractState.FullySigned.type]] =
    val party1Evidence: ContractState.Party1Signed.type =:= ContractState.Party1Signed.type = 
      ev.asInstanceOf[ContractState.Party1Signed.type =:= ContractState.Party1Signed.type]
    val contract = Contract[ContractState.Party1Signed.type](id, party1, party2)
    val next = contract.signByParty2()(using party1Evidence)
    val updated: StatefulContract[ContractState.FullySigned.type] = StatefulContract.fromContract(
      contract = next,
      party1Sig = party1Signature,
      party2Sig = SignatureStatus(true, Some(Instant.now)),
      state = ContractState.FullySigned
    )
    Try:
      updated.save().get



object StatefulContract:
  private def fromContract[S <: ContractState](contract: Contract[S], party1Sig: SignatureStatus, party2Sig: SignatureStatus, state: S): StatefulContract[S] =
    StatefulContract(contract.id, contract.party1, contract.party2, party1Sig, party2Sig, state)

  def toContract(sc: StatefulContract[? <: ContractState]): Option[Contract[? <: ContractState]] =
    sc.state match
      case ContractState.Unsigned => Some(Contract[ContractState.Unsigned.type](sc.id, sc.party1, sc.party2))
      case ContractState.Party1Signed => Some(Contract[ContractState.Party1Signed.type](sc.id, sc.party1, sc.party2))
      case ContractState.FullySigned => Some(Contract[ContractState.FullySigned.type](sc.id, sc.party1, sc.party2))

// For serialization, we need an untyped version
case class StatefulContractDto(
  id: String,
  party1: String,
  party2: String,
  party1Signature: SignatureStatus,
  party2Signature: SignatureStatus,
  state: ContractState
):
  def toTyped: StatefulContract[? <: ContractState] =
    StatefulContract(id, party1, party2, party1Signature, party2Signature, state)

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
  implicit val statefulContractDtoRW: ReadWriter[StatefulContractDto] = macroRW

  implicit def statefulContractRW[S <: ContractState]: ReadWriter[StatefulContract[S]] =
    statefulContractDtoRW.bimap[StatefulContract[S]](
      sc => StatefulContractDto(sc.id, sc.party1, sc.party2, sc.party1Signature, sc.party2Signature, sc.state),
      dto => dto.toTyped.asInstanceOf[StatefulContract[S]]
    )
}
