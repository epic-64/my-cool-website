package playground.play1

import scala.util.Try

@main def dumb =
  Try(None.get).recover{ case e => e.printStackTrace() }
  Try(List().head).recover{ case e => e.printStackTrace() }