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

lazy val kafka = ProjectRef(uri("https://github.com/Clover-Group/zio-kafka.git#master"), "zio-kafka")

lazy val front = (project in file("zio-front"))
  .settings(
    name := "front",
    commonSettings
  )
  .dependsOn(kafka)

lazy val core = (project in file("zio-core"))
  .settings(
    name := "core",
    commonSettings
  )

lazy val dsl = (project in file("zio-dsl"))
  .settings(
    name := "dsl",
    commonSettings
  )
  .dependsOn(core)

lazy val sched = (project in file("zio-sched"))
  .settings(
    name := "sched",
    commonSettings
  )
  .dependsOn(core, dsl)

lazy val top = (project in file("."))
//.enablePlugins(GitVersioning, JavaAppPackaging, UniversalPlugin)
  .settings(
    name := "tsp",
    commonSettings
  )
  .dependsOn(front, core, dsl, kafka)

scalacOptions --= Seq(
  "-Xfatal-warnings"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll test:scalafmtAll")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
addCommandAlias("cvr", "; clean; coverage; test; coverageReport")
