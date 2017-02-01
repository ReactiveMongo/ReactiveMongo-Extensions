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
import reactivemongo.bson._
import reactivemongo.api.{ DB, QueryOpts }
import reactivemongo.api.indexes.Index
import reactivemongo.api.commands.{ GetLastError, WriteResult }
import play.modules.reactivemongo.json._, collection.JSONCollection
import reactivemongo.extensions.dao.{ Dao, LifeCycle, ReflexiveLifeCycle }
import reactivemongo.extensions.json.dsl.JsonDsl._

import play.api.libs.json.{
  Json,
  JsError,
  JsSuccess,
  JsObject,
  OFormat,
  OWrites,
  Reads,
  Writes
}
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
 * import reactivemongo.play.json.BSONFormats._
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
 * import reactivemongo.play.json.BSONFormats._
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
abstract class JsonDao[Model: OFormat, ID: Writes](db: => DB, collectionName: String)(implicit lifeCycle: LifeCycle[Model, ID] = new ReflexiveLifeCycle[Model, ID], ec: ExecutionContext)
    extends Dao[JSONCollection, JsObject, Model, ID, OWrites](
      db, collectionName) {

  def ensureIndexes()(implicit ec: ExecutionContext): Future[Traversable[Boolean]] = Future sequence {
    autoIndexes map { index =>
      collection.indexesManager.ensure(index)
    }
  }.map { results =>
    lifeCycle.ensuredIndexes()
    results
  }

  def listIndexes()(implicit ec: ExecutionContext): Future[List[Index]] =
    collection.indexesManager.list()

  def findOne(selector: JsObject = Json.obj())(implicit ec: ExecutionContext): Future[Option[Model]] = collection.find(selector).one[Model]

  def findById(id: ID)(implicit ec: ExecutionContext): Future[Option[Model]] =
    findOne($id(id))

  def findByIds(ids: ID*)(implicit ec: ExecutionContext): Future[List[Model]] =
    findAll("_id" $in (ids: _*))

  def find(
    selector: JsObject = Json.obj(),
    sort: JsObject = Json.obj("_id" -> 1),
    page: Int,
    pageSize: Int)(implicit ec: ExecutionContext): Future[List[Model]] = {
    val from = (page - 1) * pageSize
    collection
      .find(selector)
      .sort(sort)
      .options(QueryOpts(skipN = from, batchSizeN = pageSize))
      .cursor[Model]()
      .collect[List](pageSize)
  }

  def findAll(
    selector: JsObject = Json.obj(),
    sort: JsObject = Json.obj("_id" -> 1))(implicit ec: ExecutionContext): Future[List[Model]] = {
    collection.find(selector).sort(sort).cursor[Model]().collect[List]()
  }

  @deprecated("0.11.1", "Directly use [[findAndUpdate]] collection operation")
  def findAndUpdate(
    query: JsObject,
    update: JsObject,
    sort: JsObject = Json.obj(),
    fetchNewObject: Boolean = false,
    upsert: Boolean = false)(implicit ec: ExecutionContext): Future[Option[Model]] = collection.findAndUpdate(
    query, update, fetchNewObject, upsert).map(_.result[Model])

  @deprecated("0.11.1", "Directly use [[findAndRemove]] collection operation")
  def findAndRemove(query: JsObject, sort: JsObject = Json.obj())(implicit ec: ExecutionContext): Future[Option[Model]] = collection.findAndRemove(
    query, if (sort == BSONDocument.empty) None else Some(sort)).
    map(_.result[Model])

  def findRandom(selector: JsObject = Json.obj())(implicit ec: ExecutionContext): Future[Option[Model]] = for {
    count <- count(selector)
    index = Random.nextInt(count)
    random <- collection.find(selector).options(QueryOpts(skipN = index, batchSizeN = 1)).one[Model]
  } yield random

  def insert(model: Model, writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val mappedModel = lifeCycle.prePersist(model)
    collection.insert(mappedModel, writeConcern) map { writeResult =>
      lifeCycle.postPersist(mappedModel)
      writeResult
    }
  }

  private val (maxBulkSize, maxBsonSize): (Int, Int) =
    Int.MaxValue -> Int.MaxValue

  def bulkInsert(
    documents: TraversableOnce[Model],
    bulkSize: Int = maxBulkSize,
    bulkByteSize: Int = maxBsonSize)(implicit ec: ExecutionContext): Future[Int] = {
    val mappedDocuments = documents.map(lifeCycle.prePersist)
    val writer = implicitly[OWrites[Model]]

    def go(docs: Traversable[Model]): Stream[JsObject] = docs.headOption match {
      case Some(doc) => writer.writes(doc) #:: go(docs.tail)
      case _ => Stream.Empty
    }

    collection.bulkInsert(go(mappedDocuments.toTraversable),
      true, defaultWriteConcern, bulkSize, bulkByteSize) map { result =>
        mappedDocuments.map(lifeCycle.postPersist)
        result.n
      }
  }

  def update[U: OWrites](
    selector: JsObject,
    update: U,
    writeConcern: GetLastError = defaultWriteConcern,
    upsert: Boolean = false,
    multi: Boolean = false)(implicit ec: ExecutionContext): Future[WriteResult] = collection.update(selector, update, writeConcern, upsert, multi)

  def updateById[U: OWrites](
    id: ID,
    update: U,
    writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[WriteResult] = collection.update($id(id), update, writeConcern)

  def save(model: Model, writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[WriteResult] = {
    val mappedModel = lifeCycle.prePersist(model)
    collection.save(mappedModel, writeConcern) map { lastError =>
      lifeCycle.postPersist(mappedModel)
      lastError
    }
  }

  def count(selector: JsObject = Json.obj())(implicit ec: ExecutionContext): Future[Int] = collection.count(Some(selector))

  def drop()(implicit ec: ExecutionContext): Future[Unit] = collection.drop()

  def dropSync(timeout: Duration = 10 seconds)(implicit ec: ExecutionContext): Unit = Await.result(drop(), timeout)

  def removeById(id: ID, writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[WriteResult] = {
    lifeCycle.preRemove(id)
    collection.remove($id(id), writeConcern = defaultWriteConcern) map { lastError =>
      lifeCycle.postRemove(id)
      lastError
    }
  }

  def remove(
    query: JsObject,
    writeConcern: GetLastError = defaultWriteConcern,
    firstMatchOnly: Boolean = false)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(query, writeConcern, firstMatchOnly)
  }

  def removeAll(writeConcern: GetLastError = defaultWriteConcern)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(query = Json.obj(), writeConcern = writeConcern, firstMatchOnly = false)
  }

  def foreach(
    selector: JsObject = Json.obj(),
    sort: JsObject = Json.obj("_id" -> 1))(f: (Model) => Unit)(implicit ec: ExecutionContext): Future[Unit] = {
    collection.find(selector).sort(sort).cursor[Model]()
      .enumerate()
      .apply(Iteratee.foreach(f))
      .flatMap(i => i.run)
  }

  def fold[A](
    selector: JsObject = Json.obj(),
    sort: JsObject = Json.obj("_id" -> 1),
    state: A)(f: (A, Model) => A)(implicit ec: ExecutionContext): Future[A] = {
    collection.find(selector).sort(sort).cursor[Model]()
      .enumerate()
      .apply(Iteratee.fold(state)(f))
      .flatMap(i => i.run)
  }

  ensureIndexes()
}

object JsonDao {
  def apply[Model: OFormat, ID: Writes](db: => DB, collectionName: String)(
    implicit lifeCycle: LifeCycle[Model, ID] = new ReflexiveLifeCycle[Model, ID], ec: ExecutionContext): JsonDao[Model, ID] = new JsonDao[Model, ID](db, collectionName) {}

}
