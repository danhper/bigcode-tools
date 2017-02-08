organization := "java-transformer"

name := "java-transformer"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.1"

libraryDependencies += "com.github.javaparser" % "javaparser-core" % "3.0.1"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.7" % "test"


scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")
scalacOptions in Test ++= Seq("-Yrangepos")
