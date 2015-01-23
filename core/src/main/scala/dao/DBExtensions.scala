package reactivemongo.extensions.dao

import play.api.libs.iteratee.Iteratee
import reactivemongo.api.QueryOpts
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.bson.BSONCountCommand
import reactivemongo.api.commands.{GetLastError, WriteConcern, WriteResult}
import reactivemongo.api.indexes.Index
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONValue}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

class DBExtensions(collection: BSONCollection) {

  def bulkInsert[Model](documents: Stream[Model], ordered: Boolean)
                       (implicit ec: ExecutionContext, writer: BSONDocumentWriter[Model]): Future[Int] = {
    collection.bulkInsert(documents.map(writer.write(_)), ordered).map(_.n)
  }

  def count(doc: BSONDocument = BSONDocument.empty)(implicit ec: ExecutionContext): Future[Int] = {
    import reactivemongo.api.commands.bson.BSONCountCommandImplicits._
    collection.runCommand(BSONCountCommand.Count(doc)).map(_.count)
  }

  def drop()(implicit ec: ExecutionContext): Future[Unit] = {
    collection.drop()
  }

  def dropSync(timeout: Duration = 10.seconds)(implicit ec: ExecutionContext): Unit = {
    Await.result(drop(), timeout)
  }

  def find[Model: BSONDocumentReader](selector: BSONDocument = BSONDocument.empty,
                                      sort: BSONDocument = BSONDocument("_id" -> 1),
                                      page: Int,
                                      pageSize: Int)
                                     (implicit ec: ExecutionContext): Future[List[Model]] = {
    val from = (page - 1) * pageSize
    collection
      .find(selector)
      .sort(sort)
      .options(QueryOpts(skipN = from, batchSizeN = pageSize))
      .cursor[Model]
      .collect[List](pageSize)
  }

  def findAll[Model: BSONDocumentReader](selector: BSONDocument = BSONDocument.empty,
                                         sort: BSONDocument = BSONDocument("_id" -> 1))
                                        (implicit ec: ExecutionContext): Future[List[Model]] = {
    collection.find(selector).sort(sort).cursor[Model].collect[List]()
  }

  def findById[Model: BSONDocumentReader](id: BSONValue)(implicit ec: ExecutionContext): Future[Option[Model]] = {
    findOne(BSONDocument("_id" -> id))
  }

  def findOne[Model: BSONDocumentReader](selector: BSONDocument = BSONDocument.empty)
                                        (implicit ec: ExecutionContext): Future[Option[Model]] = {
    collection.find(selector).one[Model]
  }

  def findRandom[Model: BSONDocumentReader](selector: BSONDocument = BSONDocument.empty)
                                           (implicit ec: ExecutionContext): Future[Option[Model]] = {
    for {
      count <- count(selector)
      index = if (count == 0) 0 else Random.nextInt(count)
      random <- collection.find(selector).options(QueryOpts(skipN = index, batchSizeN = 1)).one[Model]
    } yield random
  }

  def foreach[Model: BSONDocumentReader](selector: BSONDocument = BSONDocument.empty,
                                         sort: BSONDocument = BSONDocument("_id" -> 1))
                                        (f: (Model) => Unit)
                                        (implicit ec: ExecutionContext): Future[Unit] = {
    collection.find(selector).sort(sort).cursor[Model]
      .enumerate()
      .apply(Iteratee.foreach(f))
      .flatMap(i => i.run)
  }

  def fold[A, Model: BSONDocumentReader](state: A,
                                         selector: BSONDocument = BSONDocument.empty,
                                         sort: BSONDocument = BSONDocument("_id" -> 1))
                                        (f: (A, Model) => A)
                                        (implicit ec: ExecutionContext): Future[A] = {
    collection.find(selector).sort(sort).cursor[Model]
      .enumerate()
      .apply(Iteratee.fold(state)(f))
      .flatMap(i => i.run)
  }

  def insert[Model: BSONDocumentWriter](model: Model, writeConcern: GetLastError = WriteConcern.Default)
                                       (implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.insert(model, writeConcern)
  }

  def listIndexes()(implicit ec: ExecutionContext): Future[List[Index]] = {
    collection.indexesManager.list()
  }

  def remove(query: BSONDocument,
             writeConcern: GetLastError = WriteConcern.Default,
             firstMatchOnly: Boolean = false)
            (implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(query, writeConcern, firstMatchOnly)
  }

  def update[U: BSONDocumentWriter](selector: BSONDocument,
                                    update: U,
                                    writeConcern: GetLastError = WriteConcern.Default,
                                    upsert: Boolean = false,
                                    multi: Boolean = false)
                                   (implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update, writeConcern, upsert, multi)
  }
}
