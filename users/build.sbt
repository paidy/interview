name := "users"
version := "1.0.0"

scalaVersion := "2.12.3"
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

resolvers += Classpaths.typesafeReleases

val ScalatraVersion = "2.5.4"

libraryDependencies ++= Seq(
  // core Scalatra module, is required to run the framework
  "org.scalatra" %% "scalatra" % ScalatraVersion,

  // integrates the scalatest testing libraries
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",

  // basic logging functionality
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",

  // embedded servlet container used by the web plugin
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.19.v20190610" % "container",

  // required for building app
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",

  // spray json
  "io.spray" %%  "spray-json" % "1.3.5",

  // scalatest
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "junit" % "junit" % "4.12" % Test,

  // akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.12",
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.0",

  "com.softwaremill.quicklens" %% "quicklens" % "1.4.11",
  "org.typelevel"              %% "cats-core" % "1.0.0-MF",
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
)

enablePlugins(ScalatraPlugin)