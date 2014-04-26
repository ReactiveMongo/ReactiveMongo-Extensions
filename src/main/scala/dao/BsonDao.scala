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
import reactivemongo.bson.BsonDsl._
import reactivemongo.api.{ DB, QueryOpts }
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.{ LastError, GetLastError, Count }
import reactivemongo.extensions.model.Model
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import org.joda.time.DateTime
import Handlers._

abstract class BsonDao[T <: Model: BSONDocumentReader: BSONDocumentWriter]
    extends Dao[BSONCollection] {

  def findOne(selector: BSONDocument): Future[Option[T]] = {
    collection.find(selector).one[T]
  }

  def findById(id: Producer[BSONValue]): Future[Option[T]] = {
    findOne($id(id, idField))
  }

  /** @param page 1 based
    */
  def find(selector: BSONDocument = BSONDocument.empty,
           sort: BSONDocument = BSONDocument(idField -> 1),
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
              sort: BSONDocument = BSONDocument(idField -> 1)): Future[List[T]] = {
    collection.find(selector).sort(sort).cursor[T].collect[List]()
  }

  def findRandom(selector: BSONDocument = BSONDocument.empty): Future[Option[T]] = {
    for {
      count <- count(selector)
      index = Random.nextInt(count)
      random <- collection.find(selector).options(QueryOpts(skipN = index, batchSizeN = 1)).one[T]
    } yield random
  }

  def insert(document: T): Future[LastError] = {
    collection.insert(document)
  }

  def insert(documents: TraversableOnce[T]): Future[Int] = {
    val enumerator = Enumerator.enumerate(documents)
    collection.bulkInsert(enumerator)
  }

  def updateById(id: Producer[BSONValue],
                 update: BSONDocument,
                 writeConcern: GetLastError = GetLastError(),
                 upsert: Boolean = false,
                 multi: Boolean = false): Future[LastError] = {
    collection.update($id(id, idField), update, writeConcern, upsert, multi)
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

  // Iteratee releated APIs

  /** Iteratee.foreach */
  def foreach(selector: BSONDocument = BSONDocument.empty,
              sort: BSONDocument = BSONDocument(idField -> 1))(f: (T) => Unit): Future[Unit] = {
    collection.find(selector).sort(sort).cursor[T]
      .enumerate()
      .apply(Iteratee.foreach(f))
      .flatMap(i => i.run)
  }

  /** Iteratee.fold */
  def fold[A](selector: BSONDocument = BSONDocument.empty,
              sort: BSONDocument = BSONDocument(idField -> 1),
              state: A)(f: (A, T) => A): Future[A] = {
    collection.find(selector).sort(sort).cursor[T]
      .enumerate()
      .apply(Iteratee.fold(state)(f))
      .flatMap(i => i.run)
  }
}

