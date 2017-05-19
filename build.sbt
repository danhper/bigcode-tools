organization := "com.tuvistavie"

name := "java-ast-transformer"

version := "0.1"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.github.javaparser" % "javaparser-core" % "3.1.0-beta.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.6",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.jsuereth" %% "scala-arm" % "2.0"

)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultShellScript :+ ""))
