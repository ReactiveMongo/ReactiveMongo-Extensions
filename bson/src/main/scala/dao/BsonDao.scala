// Copyright (C) 2014 the original author or authors.
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

import scala.util.Random
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import reactivemongo.bson._
import reactivemongo.extensions.dsl.functional.BsonDsl
import reactivemongo.api.{ bulk, DB, QueryOpts }
import reactivemongo.api.indexes.Index
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.{ LastError, GetLastError, Count }
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import org.joda.time.DateTime
import Handlers._

abstract class BsonDao[T, ID](db: () => DB,
  collectionName: String)(implicit domainReader: BSONDocumentReader[T],
    domainWriter: BSONDocumentWriter[T],
    idWriter: BSONWriter[ID, _ <: BSONValue])
    extends Dao[BSONCollection, BSONDocument, T, ID, BSONDocumentWriter](db, collectionName)
    with BsonDsl {

  def ensureIndexes(): Future[Traversable[Boolean]] = Future sequence {
    autoIndexes map { index =>
      collection.indexesManager.ensure(index)
    }
  }

  def listIndexes(): Future[List[Index]] = {
    collection.indexesManager.list()
  }

  def findOne(selector: BSONDocument = BSONDocument.empty): Future[Option[T]] = {
    collection.find(selector).one[T]
  }

  def findById(id: ID): Future[Option[T]] = {
    findOne($id(id))
  }

  def findByIds(ids: Traversable[ID]): Future[List[T]] = {
    findAll("_id" $in ids)
  }

  /**
   * @param page 1 based
   */
  def find(selector: BSONDocument = BSONDocument.empty,
    sort: BSONDocument = BSONDocument("_id" -> 1),
    page: Int,
    pageSize: Int): Future[List[T]] = {
    val from = (page - 1) * pageSize
    collection
      .find(selector)
      .sort(sort)
      .options(QueryOpts(skipN = from, batchSizeN = pageSize))
      .cursor[T]
      .collect[List](pageSize)
  }

  def findAll(selector: BSONDocument = BSONDocument.empty,
    sort: BSONDocument = BSONDocument("_id" -> 1)): Future[List[T]] = {
    collection.find(selector).sort(sort).cursor[T].collect[List]()
  }

  def findRandom(selector: BSONDocument = BSONDocument.empty): Future[Option[T]] = {
    for {
      count <- count(selector)
      index = Random.nextInt(count)
      random <- collection.find(selector).options(QueryOpts(skipN = index, batchSizeN = 1)).one[T]
    } yield random
  }

  def insert(document: T, writeConcern: GetLastError = defaultWriteConcern): Future[LastError] = {
    collection.insert(document, writeConcern)
  }

  def bulkInsert(documents: TraversableOnce[T],
    bulkSize: Int = bulk.MaxDocs,
    bulkByteSize: Int = bulk.MaxBulkSize): Future[Int] = {
    val enumerator = Enumerator.enumerate(documents)
    collection.bulkInsert(enumerator, bulkSize, bulkByteSize)
  }

  def update[U: BSONDocumentWriter](selector: BSONDocument,
    update: U,
    writeConcern: GetLastError = defaultWriteConcern,
    upsert: Boolean = false,
    multi: Boolean = false): Future[LastError] = {
    collection.update(selector, update, writeConcern, upsert, multi)
  }

  def updateById[U: BSONDocumentWriter](id: ID,
    update: U,
    writeConcern: GetLastError = defaultWriteConcern): Future[LastError] = {
    collection.update($id(id), update, writeConcern)
  }

  def save(document: T, writeConcern: GetLastError = defaultWriteConcern): Future[LastError] = {
    collection.save(document, writeConcern)
  }

  def count(selector: BSONDocument = BSONDocument.empty): Future[Int] = {
    collection.db.command(Count(collectionName, Some(selector)))
  }

  def drop(): Future[Boolean] = {
    collection.drop()
  }

  def dropSync(timeout: Duration = 10 seconds): Boolean = {
    Await.result(drop(), timeout)
  }

  def removeById(id: ID, writeConcern: GetLastError = defaultWriteConcern): Future[LastError] = {
    collection.remove($id(id), writeConcern = defaultWriteConcern)
  }

  def remove(query: BSONDocument,
    writeConcern: GetLastError = defaultWriteConcern,
    firstMatchOnly: Boolean = false): Future[LastError] = {
    collection.remove(query, writeConcern, firstMatchOnly)
  }

  def removeAll(writeConcern: GetLastError = defaultWriteConcern): Future[LastError] = {
    collection.remove(query = BSONDocument.empty, writeConcern = writeConcern, firstMatchOnly = false)
  }

  // Iteratee releated APIs

  /** Iteratee.foreach */
  def foreach(selector: BSONDocument = BSONDocument.empty,
    sort: BSONDocument = BSONDocument("_id" -> 1))(f: (T) => Unit): Future[Unit] = {
    collection.find(selector).sort(sort).cursor[T]
      .enumerate()
      .apply(Iteratee.foreach(f))
      .flatMap(i => i.run)
  }

  /** Iteratee.fold */
  def fold[A](selector: BSONDocument = BSONDocument.empty,
    sort: BSONDocument = BSONDocument("_id" -> 1),
    state: A)(f: (A, T) => A): Future[A] = {
    collection.find(selector).sort(sort).cursor[T]
      .enumerate()
      .apply(Iteratee.fold(state)(f))
      .flatMap(i => i.run)
  }

  ensureIndexes()
}

