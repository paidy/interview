import sbt._

object Dependencies {

  object Versions {
    val cats           = "2.9.0"
    val catsEffect     = "3.4.8"
    val fs2            = "3.7.0"

    val scaffeine      = "5.2.1"

    val http4s         = "0.23.15"
    val circe          = "0.14.3"
    val pureConfig     = "0.17.4"

    val scalaLogging   = "3.9.5"

    val kindProjector  = "0.10.3"
    val logback        = "1.4.7"

    val scalaTest      = "3.2.15"
    val catsScalaTest  = "3.1.1"
    val scalaMock      = "5.1.0"
    val catsEffectTest = "1.5.0"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe"    %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

    // Cats and streams
    lazy val cats                = "org.typelevel"         %% "cats-core"                  % Versions.cats
    lazy val catsEffect          = "org.typelevel"         %% "cats-effect"                % Versions.catsEffect
    lazy val fs2                 = "co.fs2"                %% "fs2-core"                   % Versions.fs2

    // Cache
    lazy val scaffeine           = "com.github.blemale"    %% "scaffeine"                  % Versions.scaffeine

    // Http
    lazy val http4sDsl           = http4s("http4s-dsl")
    lazy val http4sServer        = http4s("http4s-blaze-server")
    lazy val http4sClient        = http4s("http4s-blaze-client")
    lazy val http4sCirce         = http4s("http4s-circe")

    // Json
    lazy val circeCore           = circe("circe-core")
    lazy val circeGeneric        = circe("circe-generic")
    lazy val circeGenericExt     = circe("circe-generic-extras")
    lazy val circeParser         = circe("circe-parser")

    // Config
    lazy val pureConfig          = "com.github.pureconfig" %% "pureconfig"                 % Versions.pureConfig

    // Logging
    lazy val scalaLogging        = "com.typesafe.scala-logging" %% "scala-logging"         % Versions.scalaLogging

    // Compiler plugins
    lazy val kindProjector       = "org.typelevel"         %% "kind-projector"             % Versions.kindProjector

    // Runtime
    lazy val logback             = "ch.qos.logback"        %  "logback-classic"            % Versions.logback

    // Test
    lazy val scalaTest           = "org.scalatest"         %% "scalatest"                  % Versions.scalaTest
    lazy val scalaMock           = "org.scalamock"         %% "scalamock"                  % Versions.scalaMock
    lazy val catsScalaTest       = "com.ironcorelabs"      %% "cats-scalatest"             % Versions.catsScalaTest
    lazy val catsEffectTest      = "org.typelevel"         %% "cats-effect-testing-scalatest" % Versions.catsEffectTest
  }
}
