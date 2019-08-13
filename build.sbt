val ZioVersion    = "1.0.0-RC11-1"
val Specs2Version = "4.7.0"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val commonLibs =
  libraryDependencies ++= Seq(
    "dev.zio"    %% "zio"         % ZioVersion,
    "org.specs2" %% "specs2-core" % Specs2Version % "test"
  )

lazy val commonSettings = Seq(
  organization := "CloverGroup",
  //name := "zio-tsp",
  version := "0.0.1",
  scalaVersion := "2.12.8",
  maxErrors := 3,
  parallelExecution in Test := true,
  commonLibs
)

lazy val front = (project in file("zio-front"))
  .settings(
    name := "front",
    commonSettings
  )

lazy val core = (project in file("zio-core"))
  .settings(
    name := "core",
    commonSettings
  )

lazy val top = (project in file("."))
  .settings(
    name := "tsp",
    commonSettings
  )
  .dependsOn(front)

scalacOptions --= Seq(
  "-Xfatal-warnings"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll test:scalafmtAll")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
