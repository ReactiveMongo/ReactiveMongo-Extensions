package reactivemongo.extensions

import reactivemongo.extensions.dao.DBExtensions

import scala.concurrent.{ExecutionContext, Future}

object Implicits {

  import reactivemongo.api.DefaultDB

  implicit class FutureOption[T](future: Future[Option[T]])(implicit ec: ExecutionContext) {
    def unary_~ : Future[T] = {
      future.map(_.get)
    }
  }

  implicit class DBExtensionsBuilder(db: DefaultDB) {
    def :>(collectionName: String): DBExtensions = new DBExtensions(db(collectionName))
  }

}
