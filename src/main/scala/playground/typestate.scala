package playground

import ContractState.*

enum ContractState:
  case Unsigned, Party1Signed, FullySigned

case class Contract[S <: ContractState](id: String, party1: String, party2: String):
  def signByParty1(using ev: S =:= Unsigned.type): Contract[Party1Signed.type] =
    Contract[Party1Signed.type](id, party1, party2)

  def signByParty2(using ev: S =:= Party1Signed.type): Contract[FullySigned.type] =
    Contract[FullySigned.type](id, party1, party2)

  def sendEmail(using ev: S =:= FullySigned.type): Unit =
    println(s"Email sent for contract $id to $party1 and $party2")

object Contract:
  def initialize(id: String, party1: String, party2: String): Contract[Unsigned.type] =
    Contract[Unsigned.type](id, party1, party2)

@main
def main(args: Array[String]): Unit =
  Contract.initialize("ABC123", "Alice", "Bob")
    .signByParty1
    .signByParty2
    .sendEmail