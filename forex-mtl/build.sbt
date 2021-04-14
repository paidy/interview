import Dependencies._

name := "forex"
version := "1.0.1"

scalaVersion := "2.13.2"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint:adapted-args",
  "-Xlint:byname-implicit",
  "-Xlint:inaccessible",
  "-Ydelambdafy:method",
  "-Xlog-reflective-calls",
  "-Ywarn-dead-code",
  "-Ywarn-unused:imports",
  "-Ywarn-value-discard"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  compilerPlugin(Libraries.kindProjector),
  Libraries.cats,
  Libraries.catsEffect,
  Libraries.fs2,
  Libraries.http4sDsl,
  Libraries.http4sServer,
  Libraries.http4sCirce,
  Libraries.circeCore,
  Libraries.circeGeneric,
  Libraries.circeGenericExt,
  Libraries.circeParser,
  Libraries.pureConfig,
  Libraries.logback,
  Libraries.scalaTest        % Test,
  Libraries.scalaCheck       % Test,
  Libraries.catsScalaCheck   % Test
)
