import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

name := "reactivemongo-extensions"

SbtScalariform.scalariformSettings

lazy val commonSettings = Seq(
  organization := "cn.playalot",
  version := "0.12.3",
  scalaVersion  := "2.11.11",
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8",
    "-feature",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:existentials",
    "-target:jvm-1.8"),
  resolvers ++= Seq(
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"),
  javaOptions in Test ++= Seq("-Xmx512m", "-XX:MaxPermSize=512m"),
  testOptions in Test += Tests.Argument("-oDS"),
  parallelExecution in Test := true,
  shellPrompt in ThisBuild := Common.prompt,
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
  .setPreference(IndentWithTabs, true))

lazy val publishSettings = Seq(
  organization := "cn.playalot",
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra :=
    <url>http://github.com/playalot/ReactiveMongo-Extensions</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>github.com:playalot/ReactiveMongo-Extensions.git</url>
      <connection>scm:git:github.com:playalot/ReactiveMongo-Extensions.git</connection>
    </scm>
    <developers>
      <developer>
        <id>gguan</id>
        <name>Guan Guan</name>
        <url>http://github.com/gguan</url>
      </developer>
    </developers>)

val travisSettings = Seq(
  Travis.travisSnapshotBranches := Seq("0.11.x", "0.10.5.akka23-SNAPSHOT"),
  commands += Travis.travisCommand
)

lazy val settings = (
  commonSettings
  ++ travisSettings
  ++ scalariformSettings)

lazy val root = project.in(file("."))
  .aggregate(bson, json, core, samples)
  .settings(settings: _*)
  .settings(publishSettings: _*)
  .settings(publishArtifact := false)
  .enablePlugins(ScalaUnidocPlugin)

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

lazy val samples = project.in(file("samples"))
  .settings(settings: _*)
  .settings(publishSettings: _*)
  .settings(publishArtifact := false)
  .dependsOn(core % "test->test;compile->compile", bson % "compile->compile")
