name := "scorch-akka"

version := "1.0"

scalaVersion := "2.12.5"

<<<<<<< HEAD
lazy val akkaVersion = "2.5.12"
=======
lazy val akkaVersion = "2.5.11"
>>>>>>> 8627056c73739ac86a29a50c836f8571ab906034

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "be.botkop" %% "scorch" % "0.1.0-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)


