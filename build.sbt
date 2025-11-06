ThisBuild / name         := "mycoolwebsite"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.3"
// ThisBuild / javacOptions ++= Seq("--release", "21")

enablePlugins(SbtTwirl)
enablePlugins(RevolverPlugin)
enablePlugins(JavaAppPackaging)

Compile / resourceDirectories += baseDirectory.value / "src" / "main" / "resources"
coverageEnabled      := sys.env.get("ENABLE_COVERAGE").contains("true")
executableScriptName := "main" // required by nixpacks

reStart / javaOptions ++= Seq(
  "-Xmx1024M",
  "-Xms64M",
  "-XX:SoftMaxHeapSize=128M",
  "-XX:MaxMetaspaceSize=64M",
  "-XX:+UseZGC",
  "-XX:+ZGenerational",
  "-XX:+ZUncommit",
  // "-XX:ZUncommitDelay=1",
)

val PekkoVersion     = "1.1.3"
val PekkoHttpVersion = "1.1.0"
val TapirVersion     = "1.12.2"
val UpickleVersion   = "3.3.1" // Align with tapir-json-upickle dependency to avoid eviction conflict

libraryDependencies ++= Seq(
  "org.apache.pekko"              %% "pekko-actor-typed"       % PekkoVersion,
  "org.apache.pekko"              %% "pekko-stream"            % PekkoVersion,
  "org.apache.pekko"              %% "pekko-http"              % PekkoHttpVersion,
  "org.apache.pekko"              %% "pekko-http-spray-json"   % PekkoHttpVersion,
  "com.lihaoyi"                   %% "upickle"                 % UpickleVersion,
  "com.softwaremill.sttp.client4" %% "core"                    % "4.0.0-RC1",
  "com.typesafe.play"             %% "twirl-api"               % "1.6.8",
  "com.lihaoyi"                   %% "scalatags"               % "0.13.1",

  // Tapir core + Pekko HTTP integration
  "com.softwaremill.sttp.tapir"   %% "tapir-core"              % TapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-pekko-http-server" % TapirVersion,

  // JSON (upickle) support (needed for jsonBody[WeatherResponse])
  "com.softwaremill.sttp.tapir"   %% "tapir-json-upickle"      % TapirVersion,

  // (Optional) OpenAPI generation + Swagger UI served via Pekko HTTP
  "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"      % TapirVersion,
  // Optionally add Swagger UI (uncomment if desired)
  // "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-pekko-http" % TapirVersion,

  // Testing stack
  "org.scalatest"     %% "scalatest"    % "3.2.19"   % Test,
  "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % Test,
  "org.scalamock"     %% "scalamock"    % "7.2.0"    % Test,
)
