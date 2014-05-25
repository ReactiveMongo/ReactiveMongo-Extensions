name := "reactivemongo-extensions-core"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % Common.reactiveMongoVersion,
  "com.typesafe.play" %% "play-json" % "2.2.3",
  "com.typesafe" % "config" % "1.2.1",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.6",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-classic" % "1.0.13" % "test",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test")
