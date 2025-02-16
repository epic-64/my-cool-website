ThisBuild / name         := "mycoolwebsite"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.3"

enablePlugins(SbtTwirl)
enablePlugins(RevolverPlugin)
enablePlugins(JavaAppPackaging)

Compile / resourceDirectories += baseDirectory.value / "src" / "main" / "resources"
coverageEnabled      := sys.env.get("ENABLE_COVERAGE").contains("true")
executableScriptName := "main" // required by nixpacks

val PekkoVersion     = "1.1.3"
val PekkoHttpVersion = "1.1.0"

libraryDependencies ++= Seq(
  "org.apache.pekko"              %% "pekko-actor-typed"     % PekkoVersion,
  "org.apache.pekko"              %% "pekko-stream"          % PekkoVersion,
  "org.apache.pekko"              %% "pekko-http"            % PekkoHttpVersion,
  "org.apache.pekko"              %% "pekko-http-spray-json" % PekkoHttpVersion,
  "com.lihaoyi"                   %% "upickle"               % "4.1.0",
  "com.softwaremill.sttp.client4" %% "core"                  % "4.0.0-RC1",
  "org.scalatest"                 %% "scalatest"             % "3.2.19"   % Test,
  "org.scalatestplus"             %% "mockito-5-12"          % "3.2.19.0" % Test,
  "org.scalamock"                 %% "scalamock"             % "7.2.0"    % Test,
  "com.typesafe.play"             %% "twirl-api"             % "1.6.8",
  "com.lihaoyi"                   %% "scalatags"             % "0.13.1",
)
