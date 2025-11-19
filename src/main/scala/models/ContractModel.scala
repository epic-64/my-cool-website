package models

import java.time.Instant
import upickle.default.{ReadWriter, macroRW}
import playground.{Contract, ContractState}

import scala.util.Try

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
