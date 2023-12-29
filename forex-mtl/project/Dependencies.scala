import sbt._

object Dependencies {

  object Versions {
    val cats       = "2.6.1"
    val catsEffect = "2.5.1"
    val fs2        = "2.5.4"
    val http4s     = "0.22.15"
    val circe      = "0.14.2"
    val pureConfig = "0.17.4"

    val kindProjector  = "0.13.2"
    val logback        = "1.2.3"
    val scalaCheck     = "1.15.3"
    val scalaTest      = "3.2.7"
    val catsScalaCheck = "0.3.2"
    val sttp       = "2.3.0"
    val scaffeine   = "0.28.0"
  }

  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"   %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s
    def sttp(artifact: String): ModuleID = "com.softwaremill.sttp.client" %% artifact % Versions.sttp

    lazy val cats       = "org.typelevel" %% "cats-core"   % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val fs2        = "co.fs2"        %% "fs2-core"    % Versions.fs2

    lazy val http4sDsl       = http4s("http4s-dsl")
    lazy val http4sServer    = http4s("http4s-blaze-server")
    lazy val http4sCirce     = http4s("http4s-circe")
    lazy val circeCore       = circe("circe-core")
    lazy val circeGeneric    = circe("circe-generic")
    lazy val circeGenericExt = circe("circe-generic-extras")
    lazy val circeParser     = circe("circe-parser")
    lazy val pureConfig      = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig

    //Sttp client
    lazy val sttpClientCore      = sttp("core")
    lazy val sttpClientCirce     = sttp("circe")
    lazy val sttpClientBackend   = sttp("async-http-client-backend-cats")

    //Scala caffeine for caching
    lazy val scaffiene       = "com.github.cb372" %% "scalacache-caffeine" % Versions.scaffeine

    // Compiler plugins
    lazy val kindProjector = "org.typelevel" %% "kind-projector" % Versions.kindProjector cross CrossVersion.full

    // Runtime
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

    // Test
    lazy val scalaTest      = "org.scalatest"     %% "scalatest"       % Versions.scalaTest
    lazy val scalaCheck     = "org.scalacheck"    %% "scalacheck"      % Versions.scalaCheck
    lazy val catsScalaCheck = "io.chrisdavenport" %% "cats-scalacheck" % Versions.catsScalaCheck
  }

}
