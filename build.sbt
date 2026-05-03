name := "scala-docker-app"
version := "0.1"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "cask" % "0.9.1",
  "org.postgresql" % "postgresql" % "42.7.2"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "services", _*) => MergeStrategy.filterDistinctLines
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _                        => MergeStrategy.first
}