package playground

trait Logger:
  def debug(message: String): Unit
  def info(message: String): Unit
  def error(message: String): Unit

class LiveConsoleLogger extends Logger:
  def debug(message: String): Unit = println(s"[DEBUG] $message")
  def info(message: String): Unit = println(s"[INFO]  $message")
  def error(message: String): Unit = println(s"[ERROR] $message")

class MemoryLogger extends Logger:
  import scala.collection.mutable.ListBuffer
  private val infos  = ListBuffer.empty[String]
  private val errors = ListBuffer.empty[String]
  private val debugs = ListBuffer.empty[String]

  def debug(message: String): Unit = debugs += message
  def info(message: String): Unit = infos += message
  def error(message: String): Unit = errors += message

  def debugMessages: List[String] = debugs.toList
  def infoMessages: List[String] = infos.toList
  def errorMessages: List[String] = errors.toList

