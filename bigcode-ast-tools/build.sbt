organization := "com.tuvistavie.bigcode"

name := "asttools"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.5",
  "org.apache.commons" % "commons-text" % "1.1",

  "org.scalaz" %% "scalaz-core" % "7.2.15",

  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.6",

  "com.github.scopt" %% "scopt" % "3.5.0",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25",

  "org.plotly-scala" %% "plotly-render" % "0.3.1",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",

  "com.jsuereth" %% "scala-arm" % "2.0"
)

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-Xlint")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultShellScript :+ ""))

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

bintrayReleaseOnPublish in ThisBuild := false
bintrayPackage := "bigcode-asttools"
