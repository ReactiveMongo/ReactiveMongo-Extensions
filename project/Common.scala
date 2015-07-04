import sbt._
import Keys._

object Common {
  lazy val prompt = { state: State =>
    val extracted = Project.extract(state)
    import extracted._

    (name in currentRef get structure.data).map { name =>
      "[" + Colors.blue(name) + "] $ "
    }.getOrElse("> ")
  }

  val playVersion = "2.4.0"
  val reactiveMongoVersion = "0.11.0"
  val playReactiveMongoVersion = "0.11.1.play24-SNAPSHOT"
}
