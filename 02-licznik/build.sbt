name         := "licznik"
version      := "0.1.0"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"   % "2.6.21",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.21" % Test,
  "org.scalatest"     %% "scalatest"    % "3.2.17"  % Test
)

assembly / assemblyMergeStrategy := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", _*)   => MergeStrategy.discard
  case _                          => MergeStrategy.first
}

// Stala nazwa artefaktu zamiast globa target/scala-2.13/*.jar w Dockerfile.
// Glob dopasowuje tez jar z "sbt package" i potrafi skopiowac zly plik.
assembly / assemblyJarName := "app.jar"
assembly / mainClass       := Some("Main")
