import sbt._

object Dependencies {

  object Versions {
    val cats       = "2.1.0"
    val catsEffect = "2.0.0"
    val fs2        = "2.1.0"
    val http4s     = "0.20.15"
    val circe      = "0.11.1"
    val pureConfig = "0.12.1"
    val scalacache = "0.28.0"
    val enumeratum = "1.6.1"

    val kindProjector  = "0.9.10"
    val logback        = "1.2.3"
    val scalaCheck     = "1.14.3"
    val scalaTest      = "3.1.0"
    val catsScalaCheck = "0.2.0"
  }

  object Libraries {
    def circe(artifact: String): ModuleID          = "io.circe"              %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID         = "org.http4s"            %% artifact % Versions.http4s
    def pureConfigBase(artifact: String): ModuleID = "com.github.pureconfig" %% artifact % Versions.pureConfig
    def scalaCacheBase(artifact: String): ModuleID = "com.github.cb372"      %% artifact % Versions.scalacache

    lazy val cats       = "org.typelevel" %% "cats-core"   % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val fs2        = "co.fs2"        %% "fs2-core"    % Versions.fs2

    lazy val http4sDsl          = http4s("http4s-dsl")
    lazy val http4sServer       = http4s("http4s-blaze-server")
    lazy val http4sCirce        = http4s("http4s-circe")
    lazy val http4sClient       = http4s("http4s-blaze-client")
    lazy val circeCore          = circe("circe-core")
    lazy val circeGeneric       = circe("circe-generic")
    lazy val circeGenericExt    = circe("circe-generic-extras")
    lazy val circeParser        = circe("circe-parser")
    lazy val circeJava8         = circe("circe-java8")
    lazy val pureConfig         = pureConfigBase("pureconfig")
    lazy val scalaCache         = scalaCacheBase("scalacache-core")
    lazy val scalaCacheCats     = scalaCacheBase("scalacache-cats-effect")
    lazy val scalaCacheCaffeine = scalaCacheBase("scalacache-caffeine")
    lazy val enumeratum         = "com.beachape" %% "enumeratum" % Versions.enumeratum

    // Compiler plugins
    lazy val kindProjector = "org.spire-math" %% "kind-projector" % Versions.kindProjector

    // Runtime
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

    // Test
    lazy val scalaTest      = "org.scalatest"     %% "scalatest"       % Versions.scalaTest
    lazy val scalaCheck     = "org.scalacheck"    %% "scalacheck"      % Versions.scalaCheck
    lazy val catsScalaCheck = "io.chrisdavenport" %% "cats-scalacheck" % Versions.catsScalaCheck
  }

}
