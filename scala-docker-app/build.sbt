name         := "scala-docker-app"
version      := "0.1"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.lihaoyi"  %% "cask"       % "0.9.1",
  "org.postgresql" % "postgresql" % "42.7.2",
  // Pula polaczen zamiast DriverManager na kazde zadanie.
  "com.zaxxer"     % "HikariCP"   % "5.1.0",
  // Hikari loguje przez slf4j; bez bindingu sypie ostrzezeniem na starcie.
  "org.slf4j"      % "slf4j-simple" % "2.0.13"
)

// Stala nazwa artefaktu. Wczesniej Dockerfile kopiowal target/scala-2.13/*.jar
// — glob dopasowuje tez zwykly jar z "sbt package" i potrafi wciagnac zly plik.
assembly / assemblyJarName := "app.jar"
assembly / mainClass       := Some("Main")

assembly / assemblyMergeStrategy := {
  // META-INF/services trzeba skleic, inaczej ginie rejestracja sterownika
  // JDBC i providera slf4j (oba ladowane przez ServiceLoader).
  case PathList("META-INF", "services", _*) => MergeStrategy.filterDistinctLines
  case PathList("META-INF", _*)             => MergeStrategy.discard
  case _                                    => MergeStrategy.first
}
