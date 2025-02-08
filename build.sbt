ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.3"

lazy val root = (project in file("."))
  .settings(
    name := "MyCoolWebsite"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"  % "10.5.3",
  "com.typesafe.akka" %% "akka-stream" % "2.8.8"
)