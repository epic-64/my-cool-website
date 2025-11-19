package playground

import ContractState.*

enum ContractState:
  case Unsigned, Party1Signed, FullySigned

case class Contract[S <: ContractState](id: String, party1: String, party2: String):
  def signByParty1()(using ev: S =:= Unsigned.type): Contract[Party1Signed.type] =
    Contract[Party1Signed.type](id, party1, party2)

  def signByParty2()(using ev: S =:= Party1Signed.type): Contract[FullySigned.type] =
    Contract[FullySigned.type](id, party1, party2)

  def toStatefulContract(using ev: S =:= Unsigned.type): StatefulContract[Unsigned.type] =
    StatefulContract(id, party1, party2, None, None, Unsigned)


