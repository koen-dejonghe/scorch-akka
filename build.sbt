name := "scorch-akka"

version := "1.0"

scalaVersion := "2.12.5"

lazy val akkaVersion = "2.5.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "be.botkop" %% "numsca" % "0.1.3-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
