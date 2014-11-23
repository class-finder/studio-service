name := """studio-service"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.amazonaws" % "aws-java-sdk" % "1.9.6",
  "mysql" % "mysql-connector-java" % "5.1.18"
)
