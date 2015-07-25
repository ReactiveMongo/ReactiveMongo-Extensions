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

  val playVersion = "2.3.9"
  val reactiveMongoVersion = "0.11.4"
  val playReactiveMongoVersion = "0.11.4.play23"
}
