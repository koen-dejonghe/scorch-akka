name := "scorch-akka"

version := "1.0"

scalaVersion := "2.12.5"

lazy val akkaVersion = "2.5.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "be.botkop" %% "numsca" % "0.1.4-SNAPSHOT",

  "me.tongfei" % "jtorch-cpu" % "0.3.0-SNAPSHOT",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)


