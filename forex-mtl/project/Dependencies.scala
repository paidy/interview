import sbt._

object Dependencies {

  object Versions {
    val cats       = "2.5.0"
    val catsEffect = "2.4.1"
    val fs2        = "2.5.4"
    val http4s     = "0.21.22"
    val circe      = "0.13.0"
    val enumeratum = "1.7.0"
    val pureConfig = "0.14.1"
    val doobie     = "0.13.4"

    val kindProjector  = "0.10.3"
    val logback        = "1.2.3"
    val scalaLogging = "3.9.2"
    val scalaCheck     = "1.15.3"
    val scalaTest      = "3.2.7"
    val catsScalaCheck = "0.3.0"
    val sttp           = "3.1.9"
  }

  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"                      %% artifact % Versions.circe
    def enumeratum(artifact: String): ModuleID = "com.beachape" %% artifact % Versions.enumeratum
    def http4s(artifact: String): ModuleID = "org.http4s"                    %% artifact % Versions.http4s
    def doobie(artifact: String): ModuleID = "org.tpolecat"                  %% artifact % Versions.doobie
    def sttp3(artifact: String): ModuleID  = "com.softwaremill.sttp.client3" %% artifact % Versions.sttp

    lazy val cats       = "org.typelevel" %% "cats-core"   % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val fs2        = "co.fs2"        %% "fs2-core"    % Versions.fs2

    lazy val http4sDsl        = http4s("http4s-dsl")
    lazy val http4sServer     = http4s("http4s-blaze-server")
    lazy val http4sCirce      = http4s("http4s-circe")
    lazy val circeCore        = circe("circe-core")
    lazy val circeGeneric     = circe("circe-generic")
    lazy val circeGenericExt  = circe("circe-generic-extras")

    lazy val enumeratumCore       = enumeratum("enumeratum")
    lazy val enumeratumCirce  = enumeratum("enumeratum-circe")
    lazy val circeEnumeratum  = circe("circe-generic-extras")
    lazy val circeParser      = circe("circe-parser")
    lazy val pureConfig       = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig
    lazy val doobieCore       = doobie("doobie-core")
    lazy val doobiePostgres   = doobie("doobie-postgres")
    lazy val doobieHikari     = doobie("doobie-hikari")
    lazy val sttpSlf4jBackend = sttp3("slf4j-backend")
    lazy val sttpCirce        = sttp3("circe")
    lazy val sttpFs2Client    = sttp3("async-http-client-backend-fs2")

    // Compiler plugins
    lazy val kindProjector = "org.typelevel" %% "kind-projector" % Versions.kindProjector

    // Runtime
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging"  % Versions.scalaLogging
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

    // Test
    lazy val scalaTest      = "org.scalatest"     %% "scalatest"       % Versions.scalaTest
    lazy val scalaCheck     = "org.scalacheck"    %% "scalacheck"      % Versions.scalaCheck
    lazy val catsScalaCheck = "io.chrisdavenport" %% "cats-scalacheck" % Versions.catsScalaCheck
  }

}
