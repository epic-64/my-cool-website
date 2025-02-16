ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.3"

lazy val root = (project in file("."))
  .settings(
    name := "MyCoolWebsite"
  )

enablePlugins(RevolverPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.akka"             %% "akka-http"            % "10.5.3",
  "com.typesafe.akka"             %% "akka-stream"          % "2.8.8",
  "com.typesafe.akka"             %% "akka-http-spray-json" % "10.5.3",
  "com.lihaoyi"                   %% "upickle"              % "4.1.0",
  "com.softwaremill.sttp.client4" %% "core"                 % "4.0.0-RC1",
  "org.scalatest"                 %% "scalatest"            % "3.2.19"   % Test,
  "org.scalatestplus"             %% "mockito-5-12"         % "3.2.19.0" % Test,
  "org.scalamock"                 %% "scalamock"            % "7.2.0"    % Test,
  "com.typesafe.play"             %% "twirl-api"            % "1.6.8",
  "com.lihaoyi"                   %% "scalatags"            % "0.13.1",
)

coverageEnabled := true
