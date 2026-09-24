ThisBuild / scalaVersion := "2.13.13"
ThisBuild / organization := "pl.kaamara"
ThisBuild / version      := "0.1.0"

val AkkaVersion     = "2.6.20"
val AkkaHttpVersion = "10.2.10"

lazy val root = (project in file("."))
  .settings(
    name := "akka-http-api",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed"      % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream"           % AkkaVersion,
      "com.typesafe.akka" %% "akka-http"             % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"  % AkkaHttpVersion,
      "ch.qos.logback"     % "logback-classic"       % "1.2.13",

      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion     % Test,
      "com.typesafe.akka" %% "akka-http-testkit"        % AkkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"      % AkkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.2.18"        % Test
    ),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),

    // Bez tego "sbt run" konczylby serwer od razu po starcie.
    // Teraz dziala az do Ctrl+C.
    Compile / run / fork := true,

    // Zawsze ta sama nazwa pliku, zeby Dockerfile wiedzial, co kopiowac.
    assembly / assemblyJarName := "app.jar",
    assembly / mainClass       := Some("pl.kaamara.httpapi.Main"),
    assembly / assemblyMergeStrategy := {
      // Ustawienia z kilku modulow Akki trzeba skleic, a nie nadpisac.
      case PathList("reference.conf")    => MergeStrategy.concat
      case PathList("application.conf")  => MergeStrategy.concat
      case "module-info.class"           => MergeStrategy.discard
      case x if x.endsWith("/module-info.class") => MergeStrategy.discard
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x =>
        val old = (assembly / assemblyMergeStrategy).value
        old(x)
    }
  )
