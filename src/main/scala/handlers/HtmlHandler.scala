package handlers

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

import java.nio.file.{Files, Paths}

def getFrontendVersion: Long = {
  val folders = List("src/main/resources/public/assets/css", "src/main/resources/public/assets/js")

  def getLastModified(folder: java.io.File): Long =
    val files = folder.listFiles
    if files == null || files.isEmpty then 0L
    else files.map(file => if file.isDirectory then getLastModified(file) else file.lastModified).max

  folders.map(folder => getLastModified(Paths.get(folder).toFile)).max
}

def renderHelloTwirl(name: String): String =
  views.html.hello(name, getFrontendVersion).toString

def renderPong: String =
  val emojis = List("🏓", "🏸", "🏒", "🏏", "🏑", "🏹", "🎣", "🥊", "🥋", "🥅", "🎯")
  val randomEmoji = emojis(scala.util.Random.nextInt(emojis.length))
  views.html.pong(randomEmoji).toString

extension (content: String)
  def toUtf8Http: Route = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, content))

object HtmlHandler:
  def routes: Route = concat(
    path("hello" / Segment) { name => renderHelloTwirl(name).toUtf8Http },
    path("ping") { renderPong.toUtf8Http }
  )
