import sbt._

inThisBuild(
  List(
    scalaVersion := "3.3.0",
    semanticdbEnabled := true,
    name := "users",
    version := "1.0.0"
  )
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  Dependencies.CatsCore,
  Dependencies.CatsEffect,
  Dependencies.QuickLens,
  Dependencies.Logback
) ++ Dependencies.Http4s ++
  Dependencies.Circe ++
  Dependencies.Tests
