package models

import playground.ContractState.*

import java.time.Instant
import upickle.default.{ReadWriter, macroRW}
import playground.{Contract, ContractPersistence, ContractState}

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

  def load(): Try[StatefulContract[? <: ContractState]] = ContractPersistence.load()

  def sendEmail(recipient: String, message: String)(using ev: S =:= FullySigned.type): Try[Unit] = Try:
    println(s"Sending email to $recipient: $message")

  def signByParty1()(using ev: S =:= Unsigned.type): Try[StatefulContract[Party1Signed.type]] = {
    val sig = SignatureStatus(true, Some(Instant.now))
    this.copy(party1Signature = sig, state = Party1Signed)
      .asInstanceOf[StatefulContract[Party1Signed.type]]
      .save()
  }

  def signByParty2()(using ev: S =:= Party1Signed.type): Try[StatefulContract[FullySigned.type]] = {
    val sig = SignatureStatus(true, Some(Instant.now))
    this.copy(party2Signature = sig, state = FullySigned)
      .asInstanceOf[StatefulContract[FullySigned.type]]
      .save()
  }

object StatefulContract:
  private def fromContract[S <: ContractState](contract: Contract[S], party1Sig: SignatureStatus, party2Sig: SignatureStatus, state: S): StatefulContract[S] =
    StatefulContract(contract.id, contract.party1, contract.party2, party1Sig, party2Sig, state)

  def toContract(sc: StatefulContract[? <: ContractState]): Option[Contract[? <: ContractState]] =
    sc.state match
      case Unsigned => Some(Contract[Unsigned.type](sc.id, sc.party1, sc.party2))
      case Party1Signed => Some(Contract[Party1Signed.type](sc.id, sc.party1, sc.party2))
      case FullySigned => Some(Contract[FullySigned.type](sc.id, sc.party1, sc.party2))

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
    _.toString, s => ContractState.valueOf(s)
  )
  implicit val signatureStatusRW: ReadWriter[SignatureStatus] = macroRW
  implicit val statefulContractDtoRW: ReadWriter[StatefulContractDto] = macroRW

  implicit def statefulContractRW[S <: ContractState]: ReadWriter[StatefulContract[S]] =
    statefulContractDtoRW.bimap[StatefulContract[S]](
      sc => StatefulContractDto(sc.id, sc.party1, sc.party2, sc.party1Signature, sc.party2Signature, sc.state),
      dto => dto.toTyped.asInstanceOf[StatefulContract[S]]
    )
}
