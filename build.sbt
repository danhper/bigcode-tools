organization := "com.tuvistavie"

name := "java-ast-transformer"

version := "0.1"

scalaVersion := "2.11.8"

classpathTypes += "maven-plugin"

libraryDependencies ++= Seq(
  "com.github.javaparser" % "javaparser-core" % "3.1.0-beta.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.6",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "org.deeplearning4j" % "deeplearning4j-core" % "0.8.0",
  "org.nd4j" % "nd4j-cuda-8.0-platform" % "0.8.0",
  "org.nd4j" % "nd4j-native-platform" % "0.8.0",
  "org.nd4j" %% "nd4s" % "0.8.0"
)


javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-Xlint")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultShellScript :+ ""))
