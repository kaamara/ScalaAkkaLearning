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