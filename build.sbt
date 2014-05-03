import scalariform.formatter.preferences._

name := "reactivemongo-extensions"

organization := "net.fehmicansaglam"

version := "0.10.0.1-SNAPSHOT"

scalaVersion  := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.0")

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding", "utf8",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:existentials")

parallelExecution in Test := true

testOptions in Test += Tests.Argument("-oDS")

scalariformSettings

// scalariform settings
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)

org.scalastyle.sbt.ScalastylePlugin.Settings

shellPrompt in ThisBuild := Common.prompt

resolvers ++= Seq(
  "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % Common.reactiveMongoVersion,
  "org.reactivemongo" %% "play2-reactivemongo" % Common.playReactiveMongoVersion % "provided",
  "com.typesafe.play" %% "play-json" % "2.2.2" % "provided",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.6",
  "ch.qos.logback" % "logback-classic" % "1.0.13" % "test",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test")

publishMavenStyle := true

publishArtifact in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>http://github.com/fehmicansaglam/reactivemongo-extensions</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:fehmicansaglam/reactivemongo-extensions.git</url>
    <connection>scm:git@github.com:fehmicansaglam/reactivemongo-extensions.git</connection>
  </scm>
  <developers>
    <developer>
      <id>fehmicansaglam</id>
      <name>Fehmi Can Saglam</name>
      <url>http://github.com/fehmicansaglam</url>
    </developer>
  </developers>)
