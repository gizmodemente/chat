name := "chat"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-feature","-deprecation")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.11",
  "com.typesafe.akka" %% "akka-cluster-metrics" % "2.5.11",
  "com.lightbend.akka.management" %% "akka-management" % "0.10.0",
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % "0.10.0",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "com.typesafe.akka" %% "akka-http"   % "10.1.1",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.11" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.11" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1" % Test
)