name         := "scala-docker-app"
scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
  "com.lihaoyi"   %% "cask"       % "0.9.1",
  "org.postgresql" % "postgresql" % "42.7.2",
  "org.scalatest" %% "scalatest"  % "3.2.18" % Test
)

assembly / assemblyJarName := "app.jar"
