name := "users"
version := "1.0.0"

scalaVersion := "2.12.4"

val akkaVersion = "2.5.4"
val circeVersion = "0.8.0"
val akkaHttpVersion = "10.0.10"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.softwaremill.quicklens" %% "quicklens" % "1.4.11",
  "org.typelevel"              %% "cats-core" % "1.0.0-MF",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
)
