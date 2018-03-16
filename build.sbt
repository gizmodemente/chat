name := "chat"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-feature","-deprecation")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.9" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)