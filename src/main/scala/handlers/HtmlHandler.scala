package handlers

import org.apache.pekko.http.scaladsl.model.headers.HttpCookie
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity}
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import upickle.default.*

import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.collection.concurrent.TrieMap
import scala.util.{Random, Try}

case class UserCounter(counter: Int)

object UserCounter:
  given ReadWriter[UserCounter] = macroRW

object UserCounterStorage {
  private def userFilePath(userId: String) = Paths.get(s"data/user_$userId.json")

  def readCounter(userId: String): UserCounter =
    val path = userFilePath(userId)
    if Files.exists(path) then
      val content = Files.readString(path)
      Try(read[UserCounter](content)).getOrElse(UserCounter(0))
    else UserCounter(0)

  def writeCounter(userId: String, counter: UserCounter): Unit =
    val path = userFilePath(userId)
    Files.createDirectories(path.getParent)
    Files.writeString(
      path,
      write(counter, indent = 2), // Pretty print for easy reading
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )
}

def withUserSession(innerRoute: String => Route): Route =
  optionalCookie("userId") {
    case Some(cookie) =>
      // User already has a session
      innerRoute(cookie.value)
    case None         =>
      // New user → Generate a session ID
      val userId = Random.alphanumeric.take(12).mkString
      setCookie(HttpCookie("userId", value = userId, path = Some("/"))) {
        innerRoute(userId)
      }
  }

def getFrontendVersion: Long = {
  def getLastModified(folder: java.io.File): Long =
    val files = folder.listFiles
    if files == null || files.isEmpty then 0L
    else files.map(file => if file.isDirectory then getLastModified(file) else file.lastModified).max

  List(
    "src/main/resources/public/assets/css",
    "src/main/resources/public/assets/js"
  ).map(folder => getLastModified(Paths.get(folder).toFile)).max
}

def renderHelloTwirl(name: String): String =
  views.html.hello(name, getFrontendVersion).toString

val userCounters = TrieMap[String, Int]()

def renderPong(userId: String): String = {
  val emojis = List(
    ("🏓", "#FFAADD"),
    ("🏓", "#FFBBDD"),
    ("🏓", "#FFCCDD"),
    ("🏓", "#FFDDDD"),
    ("🏓", "#FFEEDD"),
    ("🏓", "#FFDDDD"),
    ("🏓", "#FFCCDD"),
    ("🏓", "#FFBBDD"),
  )

  val current   = UserCounterStorage.readCounter(userId)
  val nextIndex = (current.counter + 1) % emojis.size

  UserCounterStorage.writeCounter(userId, UserCounter(nextIndex))

  val (emoji, color) = emojis(current.counter)
  views.html.pong(emoji, color).toString
}

extension (content: String) def toUtf8Http: Route = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, content))

object HtmlHandler:
  def routes: Route = concat(
    path("hello" / Segment)(name => renderHelloTwirl(name).toUtf8Http),
    path("ping")(withUserSession(userId => renderPong(userId).toUtf8Http))
  )
