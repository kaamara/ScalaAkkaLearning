name         := "scala-docker-app"
version      := "0.1"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.lihaoyi"  %% "cask"       % "0.9.1",
  "org.postgresql" % "postgresql" % "42.7.2",
  // Pula polaczen do bazy.
  "com.zaxxer"     % "HikariCP"   % "5.1.0",
  // Bez tego pula sypie ostrzezeniem o braku logowania.
  "org.slf4j"      % "slf4j-simple" % "2.0.13",

  "org.scalatest" %% "scalatest"   % "3.2.18" % Test
)

// Zawsze ta sama nazwa pliku, zeby Dockerfile wiedzial, co kopiowac.
assembly / assemblyJarName := "app.jar"
assembly / mainClass       := Some("Main")

assembly / assemblyMergeStrategy := {
  // Te pliki trzeba skleic, a nie nadpisac - inaczej znika sterownik
  // bazy i logowanie.
  case PathList("META-INF", "services", _*) => MergeStrategy.filterDistinctLines
  case PathList("META-INF", _*)             => MergeStrategy.discard
  case _                                    => MergeStrategy.first
}
