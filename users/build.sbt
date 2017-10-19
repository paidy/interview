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

libraryDependencies ++= Seq(
  "com.softwaremill.quicklens" %% "quicklens" % "1.4.11",
  "org.typelevel"              %% "cats-core" % "1.0.0-MF",
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
)
