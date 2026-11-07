name         := "licznik"
scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"   % "2.6.21",
  "com.typesafe.akka" %% "akka-testkit" % "2.6.21" % Test,
  "org.scalatest"     %% "scalatest"    % "3.2.18" % Test
)

assembly / assemblyJarName := "app.jar"
