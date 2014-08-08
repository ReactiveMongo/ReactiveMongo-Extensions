import sbt._
import Keys._

object Travis {
  val travisSnapshotBranches = SettingKey[Seq[String]]("branches that can be published on sonatype")

  val travisCommand = Command.command("publishSnapshotsFromTravis") { state =>
    val extracted = Project extract state
    import extracted._

    val thisRef = extracted.get(thisProjectRef)

    val isSnapshot = getOpt(version).exists(_.endsWith("SNAPSHOT"))
    val isTravisEnabled = sys.env.get("TRAVIS").exists(_ == "true")
    val isNotPR = sys.env.get("TRAVIS_PULL_REQUEST").exists(_ == "false")
    val isBranchAcceptable = sys.env.get("TRAVIS_BRANCH").exists(branch => getOpt(travisSnapshotBranches).exists(_.contains(branch)))

    if(isSnapshot && isTravisEnabled && isNotPR && isBranchAcceptable) {

      println(s"publishing $thisRef from travis...")

      val newState =
        append(
          Seq(
            publishTo := Some("Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"),
            credentials := Seq(Credentials(
              "Sonatype Nexus Repository Manager",
              "oss.sonatype.org",
              sys.env.get("SONATYPE_USER").getOrElse(throw new RuntimeException("no SONATYPE_USER defined")),
              sys.env.get("SONATYPE_PASSWORD").getOrElse(throw new RuntimeException("no SONATYPE_PASSWORD defined"))
            ))),
          state
        )

      runTask(publish in thisRef, newState)

      println(s"published $thisRef from travis")
    } else {
      println(s"not publishing $thisRef to Sonatype : isSnapshot=$isSnapshot, isTravisEnabled=$isTravisEnabled, isNotPR=$isNotPR, isBranchAcceptable=$isBranchAcceptable")
    }

    state
  }
}
