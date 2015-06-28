import Common.{ playVersion, playReactiveMongoVersion }

name := "reactivemongo-extensions-json"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % playReactiveMongoVersion,
  "com.typesafe.play" %% "play-json" % playVersion % "provided"
)
