import scalariform.formatter.preferences._

name := "reactivemongo-extensions"

organization := "org.reactivemongo"

version := "0.11.0.0"

scalaVersion  := "2.10.4"

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding", "utf8",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:existentials")

scalariformSettings

// scalariform settings
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)

org.scalastyle.sbt.ScalastylePlugin.Settings

shellPrompt in ThisBuild := Common.prompt

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % Common.reactiveMongoVersion,
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.6",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test")
