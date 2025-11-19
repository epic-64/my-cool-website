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
    val updated = this.copy(
      party1Signature = SignatureStatus(true, Some(Instant.now)),
      state = ContractState.Party1Signed
    ).asInstanceOf[StatefulContract[ContractState.Party1Signed.type]]
    updated.save()

  def signByParty2()(using ev: S =:= ContractState.Party1Signed.type): Try[StatefulContract[ContractState.FullySigned.type]] =
    val updated = this.copy(
      party2Signature = SignatureStatus(true, Some(Instant.now)),
      state = ContractState.FullySigned
    ).asInstanceOf[StatefulContract[ContractState.FullySigned.type]]
    updated.save()

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
