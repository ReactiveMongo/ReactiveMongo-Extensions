// Copyright (C) 2014 Fehmi Can Saglam (@fehmicans) and contributors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package reactivemongo.extensions.json.dao

import scala.util.Random
import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration._
import play.api.libs.json.{ Json, JsObject, Format, Writes }
import reactivemongo.bson.BSONDocument
import reactivemongo.api.{ ReadPreference, bulk, DB, QueryOpts }
import reactivemongo.api.indexes.Index
import reactivemongo.core.commands.{ LastError, GetLastError, Count, FindAndModify, Update, Remove }
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.ImplicitBSONHandlers.JsObjectWriter
import reactivemongo.extensions.BSONFormats
import reactivemongo.extensions.dao.{ Dao, LifeCycle, ReflexiveLifeCycle }
import reactivemongo.extensions.json.dsl.JsonDsl._
import play.api.libs.iteratee.{ Iteratee, Enumerator }

/**
 * A DAO implementation that operates on JSONCollection using JsObject.
 *
 * To create a DAO for a concrete model extend this class.
 *
 * Below is a sample model.
 * {{{
 * import reactivemongo.bson.BSONObjectID
 * import play.api.libs.json.Json
 * import play.modules.reactivemongo.json.BSONFormats._
 *
 * case class Person(
 *   _id: BSONObjectID = BSONObjectID.generate,
 *   name: String,
 *   surname: String,
 *   age: Int)
 *
 * object Person {
 *   implicit val personFormat = Json.format[Person]
 * }
 *
 * }}}
 *
 * To define a JsonDao for the Person model you just need to extend JsonDao.
 *
 * {{{
 * import reactivemongo.api.{ MongoDriver, DB }
 * import reactivemongo.bson.BSONObjectID
 * import play.modules.reactivemongo.json.BSONFormats._
 * import reactivemongo.extensions.json.dao.JsonDao
 * import scala.concurrent.ExecutionContext.Implicits.global
 *
 *
 * object MongoContext {
 *  val driver = new MongoDriver
 *  val connection = driver.connection(List("localhost"))
 *  def db(): DB = connection("reactivemongo-extensions")
 * }
 *
 * object PersonDao extends JsonDao[Person, BSONObjectID](MongoContext.db, "persons")
 * }}}
 *
 * @param db A parameterless function returning a [[reactivemongo.api.DB]] instance.
 * @param collectionName Name of the collection this DAO is going to operate on.
 * @param lifeCycle [[reactivemongo.extensions.dao.LifeCycle]] for the Model type.
 * @tparam Model Type of the model that this DAO uses.
 * @tparam ID Type of the ID field of the model.
 */
abstract class JsonDao[Model: Format, ID: Writes](db: => DB, collectionName: String)(implicit lifeCycle: LifeCycle[Model, ID] = new ReflexiveLifeCycle[Model, ID], ec: ExecutionContext)
    extends Dao[JSONCollection, JsObject, Model, ID, Writes](db, collectionName) {

  private def toBSONDocument(document: JsObject): BSONDocument = {
    BSONFormats.BSONDocumentFormat.reads(document).get
  }

  private def toJsObject(document: BSONDocument): JsObject = {
    BSONFormats.BSONDocumentFormat.writes(document).as[JsObject]
  }

  def ensureIndexes()(implicit ec: ExecutionContext): Future[Traversable[Boolean]] = Future sequence {
    autoIndexes map { index =>
      collection.indexesManager.ensure(index)
    }
  }.map { results =>
    lifeCycle.ensuredIndexes()
    results
  }

  def listIndexes()(implicit ec: ExecutionContext): Future[List[Index]] = {
    collection.indexesManager.list()
  }

  def findOne(selector: JsObject = Json.obj(), readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[Option[Model]] = {
    collection.find(selector).one[Model](readPreference)
  }

  def findById(id: ID, readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[Option[Model]] = {
    findOne($id(id), readPreference)
  }

  def findByIds(ids: Seq[ID], readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[List[Model]] = {
    findAll("_id" $in (ids: _*), readPreference = readPreference)
  }

  def find(
    selector: JsObject = Json.obj(),
    sort: JsObject = Json.obj("_id" -> 1),
    page: Int,
    pageSize: Int,
    readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[List[Model]] = {
    val from = (page - 1) * pageSize
    collection
      .find(selector)
      .sort(sort)
      .options(QueryOpts(skipN = from, batchSizeN = pageSize))
      .cursor[Model](readPreference)
      .collect[List](pageSize)
  }

  def findAll(
    selector: JsObject = Json.obj(),
    sort: JsObject = Json.obj("_id" -> 1),
    readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[List[Model]] = {
    collection.find(selector).sort(sort).cursor[Model](readPreference).collect[List]()
  }

  def findAndUpdate(
    query: JsObject,
    update: JsObject,
    sort: JsObject = Json.obj(),
    fetchNewObject: Boolean = false,
    upsert: Boolean = false,
    readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[Option[Model]] = {
    val command = FindAndModify(
      collection = collectionName,
      query = toBSONDocument(query),
      modify = Update(toBSONDocument(update), fetchNewObject),
      upsert = upsert,
      sort = if (sort == Json.obj()) None else Some(toBSONDocument(sort)))

    collection.db.command(command, readPreference).map(_.map(bson => implicitly[Format[Model]].reads(toJsObject(bson)).get))
  }

  def findAndRemove(
    query: JsObject,
    sort: JsObject = Json.obj(),
    readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[Option[Model]] = {
    val command = FindAndModify(
      collection = collectionName,
      query = toBSONDocument(query),
      modify = Remove,
      sort = if (sort == Json.obj()) None else Some(toBSONDocument(sort)))

    collection.db.command(command, readPreference).map(_.map(bson => implicitly[Format[Model]].reads(toJsObject(bson)).get))
  }

  def findRandom(selector: JsObject = Json.obj(), readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[Option[Model]] = {
    for {
      count <- count(selector)
      index = Random.nextInt(count)
      random <- collection.find(selector).options(QueryOpts(skipN = index, batchSizeN = 1)).one[Model](readPreference)
    } yield random
  }

  def insert(model: Model, writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[LastError] = {
    val mappedModel = lifeCycle.prePersist(model)
    collection.insert(mappedModel, writeConcern) map { lastError =>
      lifeCycle.postPersist(mappedModel)
      lastError
    }
  }

  def bulkInsert(
    documents: TraversableOnce[Model],
    bulkSize: Int = bulk.MaxDocs,
    bulkByteSize: Int = bulk.MaxBulkSize)(implicit ec: ExecutionContext): Future[Int] = {
    val mappedDocuments = documents.map(lifeCycle.prePersist)
    val enumerator = Enumerator.enumerate(mappedDocuments)
    collection.bulkInsert(enumerator, bulkSize, bulkByteSize) map { result =>
      mappedDocuments.map(lifeCycle.postPersist)
      result
    }
  }

  def update[U: Writes](
    selector: JsObject,
    update: U,
    writeConcern: GetLastError = defaultWriteConcern,
    upsert: Boolean = false,
    multi: Boolean = false)(implicit ec: ExecutionContext): Future[LastError] = {
    collection.update(selector, update, writeConcern, upsert, multi)
  }

  def updateById[U: Writes](
    id: ID,
    update: U,
    writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[LastError] = {
    collection.update($id(id), update, writeConcern)
  }

  def save(model: Model, writeConcern: GetLastError = GetLastError())(implicit ec: ExecutionContext): Future[LastError] = {
    val mappedModel = lifeCycle.prePersist(model)
    collection.save(mappedModel, writeConcern) map { lastError =>
      lifeCycle.postPersist(mappedModel)
      lastError
    }
  }

  def count(selector: JsObject = Json.obj(), readPreference: ReadPreference = defaultReadPreference)(implicit ec: ExecutionContext): Future[Int] = {
    collection.db.command(Count(collectionName, Some(JsObjectWriter.write(selector))), readPreference)
  }

  def drop()(implicit ec: ExecutionContext): Future[Boolean] = {
    collection.drop()
  }

  def dropSync(timeout: Duration = 10 seconds)(implicit ec: ExecutionContext): Boolean = {
    Await.result(drop(), timeout)
  }

  def removeById(id: ID, writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[LastError] = {
    lifeCycle.preRemove(id)
    collection.remove($id(id), writeConcern = defaultWriteConcern) map { lastError =>
      lifeCycle.postRemove(id)
      lastError
    }
  }

  def remove(
    query: JsObject,
    writeConcern: GetLastError = defaultWriteConcern,
    firstMatchOnly: Boolean = false)(implicit ec: ExecutionContext): Future[LastError] = {
    collection.remove(query, writeConcern, firstMatchOnly)
  }

  def removeAll(writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[LastError] = {
    collection.remove(query = Json.obj(), writeConcern = writeConcern, firstMatchOnly = false)
  }

  def foreach(
    selector: JsObject = Json.obj(),
    sort: JsObject = Json.obj("_id" -> 1),
    readPreference: ReadPreference = defaultReadPreference)(f: (Model) => Unit)(implicit ec: ExecutionContext): Future[Unit] = {
    collection.find(selector).sort(sort).cursor[Model](readPreference)
      .enumerate()
      .apply(Iteratee.foreach(f))
      .flatMap(i => i.run)
  }

  def fold[A](
    selector: JsObject = Json.obj(),
    sort: JsObject = Json.obj("_id" -> 1),
    state: A,
    readPreference: ReadPreference = defaultReadPreference)(f: (A, Model) => A)(implicit ec: ExecutionContext): Future[A] = {
    collection.find(selector).sort(sort).cursor[Model](readPreference)
      .enumerate()
      .apply(Iteratee.fold(state)(f))
      .flatMap(i => i.run)
  }

  ensureIndexes()
}

object JsonDao {
  def apply[Model: Format, ID: Writes](db: => DB, collectionName: String)(
    implicit lifeCycle: LifeCycle[Model, ID] = new ReflexiveLifeCycle[Model, ID],
    ec: ExecutionContext): JsonDao[Model, ID] = {
    new JsonDao[Model, ID](db, collectionName) {}
  }
}
