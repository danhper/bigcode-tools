organization := "com.tuvistavie"

name := "java-ast-transformer"

version := "0.1"

scalaVersion := "2.11.9"

libraryDependencies ++= Seq(
  "com.github.javaparser" % "javaparser-core" % "3.3.3",
  "org.scalaz" %% "scalaz-core" % "7.2.15",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.6",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "org.nd4j" % "nd4j-cuda-8.0-platform" % "0.8.0",
  "org.nd4j" % "nd4j-native-platform" % "0.8.0",
  "org.nd4j" %% "nd4s" % "0.8.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.plotly-scala" %% "plotly-render" % "0.3.1"
)

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-Xlint")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultShellScript :+ ""))
