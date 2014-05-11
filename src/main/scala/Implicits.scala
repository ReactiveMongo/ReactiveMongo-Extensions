package reactivemongo.extensions

import scala.concurrent.{ Future, ExecutionContext }

object Implicits {

  implicit class FutureOption[T](future: Future[Option[T]])(implicit ec: ExecutionContext) {

    def unary_~ : Future[T] = {
      future.map(_.get)
    }
  }

}
