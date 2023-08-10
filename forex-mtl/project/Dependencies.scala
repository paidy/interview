import sbt._

object Dependencies {

  object Versions {
    val cats        = "2.5.0"
    val catsEffect  = "2.5.3"
    val fs2         = "2.5.4"
    val sttpBackend = "3.3.6"
    val http4s      = "0.21.22"
    val circe       = "0.13.0"
    val pureConfig  = "0.14.1"
    val scalaCache  = "0.28.0"
    val enumeratum  = "1.7.3"

    val kindProjector  = "0.10.3"
    val logback        = "1.2.3"
    val scalaCheck     = "1.15.3"
    val scalaTest      = "3.2.7"
    val catsScalaCheck = "0.3.0"
    val testContainers = "0.40.12"
    val scalatestCats  = "0.5.4"
  }

  object Libraries {
    def circe(artifact: String): ModuleID       = "io.circe"                      %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID      = "org.http4s"                    %% artifact % Versions.http4s
    def scalaCache(artifact: String): ModuleID  = "com.github.cb372"              %% artifact % Versions.scalaCache
    def sttpBackend(artifact: String): ModuleID = "com.softwaremill.sttp.client3" %% artifact % Versions.sttpBackend

    lazy val cats           = "org.typelevel" %% "cats-core" % Versions.cats
    lazy val catsEffect     = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val fs2            = "co.fs2" %% "fs2-core" % Versions.fs2
    lazy val fs2SttpBackend = sttpBackend("async-http-client-backend-fs2-ce2")
    lazy val sttpCirce      = sttpBackend("circe")
    lazy val enumeratum     = "com.beachape" %% "enumeratum" % Versions.enumeratum

    lazy val scalaCacheEffect   = scalaCache("scalacache-cats-effect")
    lazy val scalaCacheCaffeine = scalaCache("scalacache-caffeine")

    lazy val http4sDsl       = http4s("http4s-dsl")
    lazy val http4sServer    = http4s("http4s-blaze-server")
    lazy val http4sCirce     = http4s("http4s-circe")
    lazy val circeCore       = circe("circe-core")
    lazy val circeGeneric    = circe("circe-generic")
    lazy val circeGenericExt = circe("circe-generic-extras")
    lazy val circeParser     = circe("circe-parser")
    lazy val pureConfig      = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig

    // Compiler plugins
    lazy val kindProjector = "org.typelevel" %% "kind-projector" % Versions.kindProjector

    // Runtime
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

    // Test
    lazy val scalaTest           = "org.scalatest"     %% "scalatest"                      % Versions.scalaTest
    lazy val scalaCheck          = "org.scalacheck"    %% "scalacheck"                     % Versions.scalaCheck
    lazy val catsScalaCheck      = "io.chrisdavenport" %% "cats-scalacheck"                % Versions.catsScalaCheck
    lazy val testContainers      = "com.dimafeng"      %% "testcontainers-scala-scalatest" % Versions.testContainers
    lazy val catsEffectScalaTest = "com.codecommit"    %% "cats-effect-testing-scalatest"  % Versions.scalatestCats
  }

}
