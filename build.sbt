name := "playrest"
organization := "com.codingrodent"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.5"

libraryDependencies += guice
//
// add test support
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
//
// add support for swagger / play 2
libraryDependencies += "com.github.dwickern" %% "swagger-play2.8" % "3.1.0"
//
// the swagger ui
libraryDependencies += "org.webjars" % "swagger-ui" % "3.44.0"
//
// json handling
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.1"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.1"
//
// annotation support
libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1"
