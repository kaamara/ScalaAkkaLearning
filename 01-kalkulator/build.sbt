name         := "kalkulator"
version      := "0.1.0"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"   % "2.6.21",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.21" % Test,
  "org.scalatest"     %% "scalatest"    % "3.2.17"  % Test
)
// Sklejanie kilku bibliotek w jeden plik .jar. Pliki o tych samych
// nazwach trzeba potraktowac osobno, inaczej assembly zglasza konflikt.
assembly / assemblyMergeStrategy := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", _*)   => MergeStrategy.discard
  case _                          => MergeStrategy.first
}

// Zawsze ta sama nazwa pliku, zeby Dockerfile wiedzial, co kopiowac.
assembly / assemblyJarName := "app.jar"
assembly / mainClass       := Some("Main")
