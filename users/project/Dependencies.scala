import sbt._

object Dependencies {

  object Versions {
    val catsCore = "2.9.0"
    val catsEffect = "3.5.1"
    val quckLens = "1.9.6"
    val http4sVersion = "1.0.0-M40"
    val logback = "1.2.3"
    val circe = "0.14.5"

    val catsEffectScalatest = "1.5.0"
    val scalatest = "3.2.16"
  }

  lazy val CatsCore = "org.typelevel"               %% "cats-core"       % Versions.catsCore
  lazy val CatsEffect = "org.typelevel"             %% "cats-effect"     % Versions.catsEffect
  lazy val QuickLens = "com.softwaremill.quicklens" %% "quicklens"       % Versions.quckLens
  lazy val Logback = "ch.qos.logback"                % "logback-classic" % Versions.logback

  lazy val Http4s = Seq(
    "org.http4s" %% "http4s-ember-client",
    "org.http4s" %% "http4s-ember-server",
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-circe"
  ).map(_ % Versions.http4sVersion)

  lazy val Circe = Seq(
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-literal"
  ).map(_ % Versions.circe)

  lazy val Tests = Seq(
    "org.typelevel" %% "cats-effect-testing-scalatest" % Versions.catsEffectScalatest,
    "org.scalatest" %% "scalatest"                     % Versions.scalatest
  ).map(_ % Test)
}
