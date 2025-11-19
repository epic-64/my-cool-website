package playground

import playground.ContractState.*
import playground.{Contract, ContractPersistence, ContractState}
import upickle.default.{ReadWriter, macroRW}

import java.time.Instant
import scala.util.Try

case class StatefulContract[S <: ContractState](
  id: String,
  party1: String,
  party2: String,
  party1Signature: Option[Instant],
  party2Signature: Option[Instant],
  state: S
):
  def save(): Try[StatefulContract[S]] = playground.ContractPersistence.save(this).map(_ => this)

  def load(): Try[StatefulContract[? <: ContractState]] = ContractPersistence.load()

  def sendEmail(recipient: String, message: String)(using ev: S =:= FullySigned.type): Try[Unit] = Try:
    println(s"Sending email to $recipient: $message")

  def signByParty1()(using ev: S =:= Unsigned.type): Try[StatefulContract[Party1Signed.type]] =
    this.copy(party1Signature = Some(Instant.now), state = Party1Signed)
      .asInstanceOf[StatefulContract[Party1Signed.type]]
      .save()

  def signByParty2()(using ev: S =:= Party1Signed.type): Try[StatefulContract[FullySigned.type]] =
    this.copy(party2Signature = Some(Instant.now), state = FullySigned)
      .asInstanceOf[StatefulContract[FullySigned.type]]
      .save()

object StatefulContract:
  private def fromContract[S <: ContractState](contract: Contract[S], party1Sig: Option[Instant], party2Sig: Option[Instant], state: S): StatefulContract[S] =
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
  party1Signature: Option[Instant],
  party2Signature: Option[Instant],
  state: ContractState
):
  def toTyped: StatefulContract[? <: ContractState] =
    StatefulContract(id, party1, party2, party1Signature, party2Signature, state)

object ContractModelRW:
  implicit val instantRW: ReadWriter[Instant] = upickle.default.readwriter[String].bimap[Instant](
    _.toString,
    Instant.parse
  )
  implicit val contractStateRW: ReadWriter[ContractState] = upickle.default.readwriter[String].bimap[ContractState](
    _.toString, s => ContractState.valueOf(s)
  )
  implicit val statefulContractDtoRW: ReadWriter[StatefulContractDto] = macroRW

  implicit def statefulContractRW[S <: ContractState]: ReadWriter[StatefulContract[S]] =
    statefulContractDtoRW.bimap[StatefulContract[S]](
      sc => StatefulContractDto(sc.id, sc.party1, sc.party2, sc.party1Signature, sc.party2Signature, sc.state),
      dto => dto.toTyped.asInstanceOf[StatefulContract[S]]
    )

