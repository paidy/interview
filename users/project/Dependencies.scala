import sbt._

object Dependencies {

  object Versions {
    val catsCore = "2.9.0"
    val catsEffect = "3.5.1"
    val quckLens = "1.9.6"
  }

  lazy val CatsCore = "org.typelevel"               %% "cats-core"   % Versions.catsCore
  lazy val CatsEffect = "org.typelevel"             %% "cats-effect" % Versions.catsEffect
  lazy val QuickLens = "com.softwaremill.quicklens" %% "quicklens"   % Versions.quckLens
}
