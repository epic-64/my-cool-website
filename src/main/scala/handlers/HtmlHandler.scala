package handlers

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

import java.nio.file.{Files, Paths}

def cssVersionTimestamp: Long =
  val path = Paths.get("src/main/resources/public/assets/css/style.css")
  Files.getLastModifiedTime(path).toMillis

def renderHelloTwirl(name: String): String =
  def cssVersion = cssVersionTimestamp
  views.html.hello(name, cssVersion).toString

def renderPong: String =
  val emojis = List("🏓", "🏸", "🏒", "🏏", "🏑", "🏹", "🎣", "🥊", "🥋", "🥅", "🎯")
  val randomEmoji = emojis(scala.util.Random.nextInt(emojis.length))
  views.html.pong(randomEmoji).toString

extension (content: String)
  def toUtf8Http: Route = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, content))

object HtmlHandler:
  def routes: Route = concat(
    path("hello-twirl" / Segment) { name => renderHelloTwirl(name).toUtf8Http },
    path("ping") { renderPong.toUtf8Http }
  )
