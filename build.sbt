import scalariform.formatter.preferences._

name := "reactivemongo-extensions"

lazy val commonSettings = Seq(
  organization := "org.reactivemongo",
  version := "0.10.0.0-SNAPSHOT",
  scalaVersion  := "2.10.4",
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8",
    "-feature",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:existentials"),
  resolvers ++= Seq(
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"),
  parallelExecution in Test := true,
  javaOptions in Test ++= Seq("-Xmx512m", "-XX:MaxPermSize=512m"),
  testOptions in Test += Tests.Argument("-oDS"),
  shellPrompt in ThisBuild := Common.prompt,
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true))

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
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
      <developer>
        <id>osxhacker</id>
        <name>Steve Vickers</name>
        <url>http://github.com/osxhacker</url>
      </developer>
    </developers>))


lazy val settings = (
  commonSettings
  ++ scalariformSettings
  ++ org.scalastyle.sbt.ScalastylePlugin.Settings)

lazy val root = project.in(file("."))
  .aggregate(bson, json, core)
  .settings(settings: _*)
  .settings(unidocSettings: _*)
  .settings(publishArtifact := false)

lazy val core = project.in(file("core"))
  .settings(settings: _*)
  .settings(publishSettings: _*)

lazy val bson = project.in(file("bson"))
  .settings(settings: _*)
  .settings(publishSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val json = project.in(file("json"))
  .settings(settings: _*)
  .settings(publishSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

