package models

import spray.json.DefaultJsonProtocol.*
import spray.json.RootJsonFormat

case class SumResult(x: Int, y: Int, sum: Int)
case class MultiplyRequest(x: Int, y: Int)
case class MultiplyResult(x: Int, y: Int, product: Int)

object MathModels:
  given RootJsonFormat[SumResult]       = jsonFormat3(SumResult.apply)
  given RootJsonFormat[MultiplyRequest] = jsonFormat2(MultiplyRequest.apply)
  given RootJsonFormat[MultiplyResult]  = jsonFormat3(MultiplyResult.apply)
