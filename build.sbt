name := "reactivemongo-extensions"

lazy val commonSettings = Seq(
  organization := "org.reactivemongo",
  version := "0.11.0.0-SNAPSHOT",
  scalaVersion  := "2.11.5",
  scalacOptions := Seq(
    "-deprecation",
    "-encoding", "UTF-8",       // yes, this is 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import"     // 2.11 only
  ),
  resolvers ++= Seq(
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"),
  parallelExecution in Test := false,
  javaOptions in Test ++= Seq("-Xmx512m", "-XX:MaxPermSize=512m"),
  testOptions in Test += Tests.Argument("-oDS"),
  shellPrompt in ThisBuild := Common.prompt)

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

lazy val root = project.in(file("."))
  .aggregate(bson, core)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(publishArtifact := false)
  .settings(unidocSettings: _*)

lazy val core = project.in(file("core"))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)

lazy val bson = project.in(file("bson"))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .dependsOn(core % "test->test;compile->compile")
