package playground

trait Logger:
  def info(message: String): Unit
  def error(message: String): Unit

class LiveConsoleLogger extends Logger:
  def info(message: String): Unit = println(s"[INFO]  $message")
  def error(message: String): Unit = println(s"[ERROR] $message")

class MemoryLogger extends Logger:
  import scala.collection.mutable.ListBuffer
  private val infos  = ListBuffer.empty[String]
  private val errors = ListBuffer.empty[String]
  def info(message: String): Unit = infos += message
  def error(message: String): Unit = errors += message
  def infoMessages: List[String] = infos.toList
  def errorMessages: List[String] = errors.toList

