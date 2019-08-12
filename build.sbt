val ZioVersion      = "1.0.0-RC11-1"
val ZIOKafkaVersion = "0.0.1"
val Specs2Version   = "4.7.0"
val ArrowVersion    = "0.14.1"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val commonLibs =
  libraryDependencies ++= Seq(
    "dev.zio"          %% "zio"         % ZioVersion,
    "dev.zio"          %% "zio-kafka"   % ZIOKafkaVersion,
    "org.specs2"       %% "specs2-core" % Specs2Version % "test",
    "org.apache.arrow" % "arrow-vector" % ArrowVersion
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

lazy val front = (project in file("zio_front"))
  .settings(
    name := "front",
    commonSettings
  )

//lazy val serdes = (project in file("zio-serdes"))
//  .settings(
//    name := "serdes",
//    commonSettings
//  )

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
