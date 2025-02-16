package handlers

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import scalatags.Text.all.{head as htmlHead, title as _, *}
import scalatags.Text.tags2.title

def layout(pageTitle: String)(htmlContent: Modifier*): String =

  val theHead = htmlHead(
    title(pageTitle),
    meta(charset := "UTF-8"),
    meta(name := "viewport", content := "width=device-width, initial-scale=1"),
    // raw("""<meta name="viewport" content="width=device-width, initial-scale=1">"""),
    link(rel := "stylesheet", href := "/assets/css/style.css"),
    link(rel := "stylesheet", href := "https://unpkg.com/@tailwindcss/browser@4"),
    script(src := "https://unpkg.com/htmx.org@2.0.0/dist/htmx.min.js")
  )

  val theBody = body(
    header(
      h1("My Cool Website")
    ),
    div(cls := "container")(htmlContent), // Content provided by the route
    footer(
      p("© 2025 My Cool Website")
    )
  )

  html(theHead, theBody).render

def renderHelloPage(name: String): String =
  layout(s"Hello $name")(
    div(cls := "max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-md space-y-4")(
      h2(cls := "text-2xl font-bold text-gray-800")(s"Hello, $name!"),
      p(cls := "text-gray-600")("Welcome to our website."),
      button(
        cls := "bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded",
        attr("hx-get") := "/ping",
        attr("hx-swap") := "outerHTML"
      )("Ping the server!!")
    )
  )

def renderHelloTwirl(name: String): String = views.html.hello(name).toString

extension (content: String)
  def toUtf8Http: Route = complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, content))

object HtmlHandler:
  def routes: Route = concat(
    path("hello" / Segment) { name => renderHelloPage(name).toUtf8Http },
    path("hello-twirl" / Segment) { name => renderHelloTwirl(name).toUtf8Http },
    path("ping") { views.html.pong().toString.toUtf8Http }
  )
