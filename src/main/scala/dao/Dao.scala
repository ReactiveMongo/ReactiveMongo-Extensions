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

import reactivemongo.api.{ DB, Collection, CollectionProducer }
import reactivemongo.api.indexes.Index
import reactivemongo.bson.BSONObjectID
import reactivemongo.core.commands.{ LastError, GetLastError }
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration.Duration
import scala.language.higherKinds

abstract class Dao[C <: Collection: CollectionProducer, Structure, Model, ID, Writer[_]](db: () => DB, collectionName: String) {

  implicit def ec: ExecutionContext = ExecutionContext.Implicits.global

  def autoIndexes: Traversable[Index] = Seq.empty

  def bulkInsert(models: TraversableOnce[Model], bulkSize: Int, bulkByteSize: Int): Future[Int]

  def collection: C = db().collection[C](collectionName)

  def count(selector: Structure): Future[Int]

  def defaultWriteConcern: GetLastError = GetLastError()

  def drop(): Future[Boolean]

  def dropSync(timeout: Duration): Boolean

  def ensureIndexes(): Future[Traversable[Boolean]]

  /** @param page 1 based
    */
  def find(selector: Structure, sort: Structure, page: Int, pageSize: Int): Future[List[Model]]

  def findAll(selector: Structure, sort: Structure): Future[List[Model]]

  def findById(id: ID): Future[Option[Model]]

  def findByIds(ids: Traversable[ID]): Future[List[Model]]

  def findOne(selector: Structure): Future[Option[Model]]

  def findRandom(selector: Structure): Future[Option[Model]]

  /** Iteratee.fold */
  def fold[A](selector: Structure,
              sort: Structure,
              state: A)(f: (A, Model) => A): Future[A]

  /** Iteratee.foreach */
  def foreach(selector: Structure,
              sort: Structure)(f: (Model) => Unit): Future[Unit]

  def insert(model: Model, writeConcern: GetLastError): Future[LastError]

  def listIndexes(): Future[List[Index]]

  def remove(query: Structure,
             writeConcern: GetLastError,
             firstMatchOnly: Boolean): Future[LastError]

  def removeAll(writeConcern: GetLastError): Future[LastError]

  def removeById(id: ID, writeConcern: GetLastError): Future[LastError]

  def save(model: Model, writeConcern: GetLastError): Future[LastError]

  def update[U: Writer](selector: Structure,
                        update: U,
                        writeConcern: GetLastError,
                        upsert: Boolean,
                        multi: Boolean): Future[LastError]

  def updateById[U: Writer](id: ID, update: U, writeConcern: GetLastError): Future[LastError]
}
