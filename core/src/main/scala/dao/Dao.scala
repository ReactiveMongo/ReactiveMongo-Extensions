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

package reactivemongo.extensions.dao

import reactivemongo.api.{ DB, Collection, CollectionProducer }
import reactivemongo.api.indexes.Index
import reactivemongo.core.commands.{ LastError, GetLastError }
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration.Duration

/**
 * Base class for all DAO implementations. This class defines the API for all DAOs.
 *
 * A DAO defines how to work with a specific collection.
 *
 * @param db A parameterless function returning a [[reactivemongo.api.DB]] instance.
 * @param collectionName Name of the collection this DAO is going to operate on.
 * @tparam C Type of the collection.
 * @tparam Structure The type that C operates on. `BSONDocument` or `JsObject`.
 * @tparam Model Type of the model that this DAO uses.
 * @tparam ID Type of the ID field of the model.
 * @tparam Writer
 */
abstract class Dao[C <: Collection: CollectionProducer, Structure, Model, ID, Writer[_]](db: () => DB, collectionName: String) {

  /**
   * Defines the [[scala.concurrent.ExecutionContext]] for this DAO.
   *
   * Defaults to [[scala.concurrent.ExecutionContext.Implicits.global]].
   */
  implicit def ec: ExecutionContext = ExecutionContext.Implicits.global

  /**
   * The list of indexes to be ensured on DAO load.
   *
   * Because of Scala initialization order there are exactly 2 ways
   * of defining auto indexes.
   *
   * First way is to use an '''early definition''':
   *
   * {{{
   * object PersonDao extends {
   *   override val autoIndexes = Seq(
   *     Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
   *     Index(Seq("age" -> IndexType.Ascending), background = true))
   * } with BsonDao[Person, BSONObjectID](MongoContext.db, "persons")
   * }}}
   *
   * Second way is to '''override def'''. Be careful __not to change declaration to `val` instead of `def`__.
   *
   * {{{
   * object PersonDao extends BsonDao[Person, BSONObjectID](MongoContext.db, "persons") {
   *
   *   override def autoIndexes = Seq(
   *     Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
   *     Index(Seq("age" -> IndexType.Ascending), background = true))
   * }
   * }}}
   */
  def autoIndexes: Traversable[Index] = Seq.empty

  /**
   * Bulk inserts multiple models.
   *
   * @param models A [[scala.collection.TraversableOnce]] of models.
   * @param bulkSize
   * @param bulkByteSize
   * @return The number of successful insertions.
   */
  def bulkInsert(models: TraversableOnce[Model], bulkSize: Int, bulkByteSize: Int): Future[Int]

  /** Reference to the collection this DAO operates on. */
  def collection: C = db().collection[C](collectionName)

  /**
   * Returns the number of documents in this collection matching the given selector.
   *
   * @param selector Selector document which may be empty.
   */
  def count(selector: Structure): Future[Int]

  /**
   * Defines the default write concern for this Dao which defaults to `GetLastError()`.
   *
   * Related API functions should allow overriding this value.
   */
  def defaultWriteConcern: GetLastError = GetLastError()

  /** Drops this collection */
  def drop(): Future[Boolean]

  /**
   * Drops this collection and awaits until it has been dropped or a timeout has occured.
   * @param timeout Maximum amount of time to await until this collection has been dropped.
   * @return true if the collection has been successfully dropped, otherwise false.
   */
  def dropSync(timeout: Duration): Boolean

  /** Ensures indexes defined by `autoIndexes`. */
  def ensureIndexes(): Future[Traversable[Boolean]]

  /**
   * Retrieves models by page matching the given selector.
   *
   * @param selector Selector document.
   * @param sort Sorting document.
   * @param page 1 based page number.
   * @param pageSize Maximum number of elements in each page.
   */
  def find(selector: Structure, sort: Structure, page: Int, pageSize: Int): Future[List[Model]]

  /**
   * Retrieves all models matching the given selector.
   *
   * @param selector Selector document.
   * @param sort Sorting document.
   */
  def findAll(selector: Structure, sort: Structure): Future[List[Model]]

  /** Retrieves the model with the given `id`. */
  def findById(id: ID): Future[Option[Model]]

  /** Retrieves the models with the given `ids`. */
  def findByIds(ids: Traversable[ID]): Future[List[Model]]

  /** Retrieves at most one model matching the given selector. */
  def findOne(selector: Structure): Future[Option[Model]]

  /**
   * Retrieves a random model matching the given selector.
   *
   * This API may require more than one query.
   */
  def findRandom(selector: Structure): Future[Option[Model]]

  /**
   * Folds the documents matching the given selector by applying the function `f`.
   *
   * @param selector Selector document.
   * @param sort Sorting document.
   * @param state Initial state for the fold operation.
   * @param f Folding function.
   * @tparam A Type of fold result.
   */
  def fold[A](selector: Structure, sort: Structure, state: A)(f: (A, Model) => A): Future[A]

  /**
   * Iterates over the documents matching the given selector and applies the function `f`.
   *
   * @param selector Selector document.
   * @param sort Sorting document.
   * @param f function to be applied.
   */
  def foreach(selector: Structure, sort: Structure)(f: (Model) => Unit): Future[Unit]

  /** Inserts the given model. */
  def insert(model: Model, writeConcern: GetLastError): Future[LastError]

  /**
   * Lists indexes that are currently ensured in this collection.
   *
   * This list may not be equal to `autoIndexes` in case of index creation failure.
   */
  def listIndexes(): Future[List[Index]]

  /**
   * Removes model(s) matching the given selector.
   *
   * In order to remove multiple documents `firstMatchOnly` has to be `false`.
   *
   * @param selector Selector document.
   * @param writeConcern Write concern defaults to `defaultWriteConcern`.
   * @param firstMatchOnly Remove only the first matching document.
   */
  def remove(
    selector: Structure,
    writeConcern: GetLastError,
    firstMatchOnly: Boolean): Future[LastError]

  /** Removes all documents in this collection. */
  def removeAll(writeConcern: GetLastError): Future[LastError]

  /** Removes the document with the given ID. */
  def removeById(id: ID, writeConcern: GetLastError): Future[LastError]

  /**
   * Inserts the document, or updates it if it already exists in the collection.
   *
   * @param model The model to save.
   * @param writeConcern the [[reactivemongo.core.commands.GetLastError]] command message to send in order to control
   *                     how the document is inserted. Defaults to defaultWriteConcern.
   */
  def save(model: Model, writeConcern: GetLastError): Future[LastError]

  /**
   * Updates the documents matching the given selector.
   *
   * @param selector Selector query.
   * @param update Update query.
   * @param writeConcern Write concern which defaults to defaultWriteConcern.
   * @param upsert Create the document if it does not exist.
   * @param multi Update multiple documents.
   * @tparam U Type of the update query.
   */
  def update[U: Writer](
    selector: Structure,
    update: U,
    writeConcern: GetLastError,
    upsert: Boolean,
    multi: Boolean): Future[LastError]

  /**
   * Updates the document with the given `id`.
   *
   * @param id ID of the document that will be updated.
   * @param update Update query.
   * @param writeConcern Write concern which defaults to defaultWriteConcern.
   * @tparam U Type of the update query.
   */
  def updateById[U: Writer](id: ID, update: U, writeConcern: GetLastError): Future[LastError]
}
