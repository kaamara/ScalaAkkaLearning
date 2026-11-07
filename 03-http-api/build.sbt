name         := "akka-http-api"
scalaVersion := "2.13.13"

val AkkaVersion     = "2.6.21"
val AkkaHttpVersion = "10.2.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"         % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream"              % AkkaVersion,
  "com.typesafe.akka" %% "akka-http"                % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json"     % AkkaHttpVersion,
  "org.slf4j"          % "slf4j-simple"             % "1.7.36",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion     % Test,
  "com.typesafe.akka" %% "akka-http-testkit"        % AkkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"      % AkkaVersion     % Test,
  "org.scalatest"     %% "scalatest"                % "3.2.18"        % Test
)

Compile / run / fork := true

assembly / assemblyJarName := "app.jar"
