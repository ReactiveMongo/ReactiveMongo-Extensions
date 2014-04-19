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

import scala.concurrent.Future
import reactivemongo.bson.{ BSONDocument, BSONDocumentReader, BSONDocumentWriter }
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.{ LastError, Count }
import reactivemongo.extensions.model.Model
import play.api.libs.iteratee.Iteratee

abstract class BsonDao[T <: Model: BSONDocumentReader: BSONDocumentWriter] extends Dao[BSONCollection] {

  def findById(id: String): Future[Option[T]] = {
    findOne(BSONDocument("id" -> id))
  }

  def findOne(selector: BSONDocument): Future[Option[T]] = {
    collection.find(selector).one[T]
  }

  def insert(document: BSONDocument): Future[LastError] = {
    collection.insert(document)
  }

  def insert(document: T): Future[LastError] = {
    collection.insert(document)
  }

  def updateById(id: String, selector: BSONDocument): Future[LastError] = {
    collection.update(BSONDocument("id" -> id), selector)
  }

  def count(selector: Option[BSONDocument] = None): Future[Int] = {
    collection.db.command(Count(collectionName, selector))
  }

  def foreach(selector: BSONDocument = BSONDocument.empty,
              sort: BSONDocument = BSONDocument("_id" -> 1))(f: (T) => Unit): Future[Unit] = {
    collection.find(selector).sort(sort).cursor[T]
      .enumerate()
      .apply(Iteratee.foreach(f))
      .flatMap(i => i.run)
  }

  def fold[A](selector: BSONDocument = BSONDocument.empty,
              sort: BSONDocument = BSONDocument("_id" -> 1),
              state: A)(f: (A, T) => A): Future[A] = {
    collection.find(selector).sort(sort).cursor[T]
      .enumerate()
      .apply(Iteratee.fold(state)(f))
      .flatMap(i => i.run)
  }
}

