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
import play.api.libs.json.{ Json, JsObject, OFormat }
import reactivemongo.bson.BSONObjectID
import reactivemongo.api.QueryOpts
import reactivemongo.core.commands.{ LastError, GetLastError, Count }
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.ImplicitBSONHandlers.JsObjectWriter
import reactivemongo.extensions.model.Model
import play.api.libs.iteratee.{ Iteratee, Enumerator }

abstract class JsonDao[T <: Model: OFormat] extends Dao[JSONCollection] {

  def findOne(selector: JsObject): Future[Option[T]] = {
    collection.find(selector).one[T]
  }

  def findById(id: BSONObjectID): Future[Option[T]] = {
    findOne(Json.obj("_id" -> id))
  }

  def findRandom(selector: JsObject = Json.obj()): Future[Option[T]] = {
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

  def updateById(id: BSONObjectID, query: JsObject): Future[LastError] =
    collection.update(Json.obj("_id" -> id), query)

  /** @page 1 based
    */
  def scroll(selector: JsObject = Json.obj(),
             sort: JsObject = Json.obj("_id" -> 1),
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

  def count(selector: JsObject = Json.obj()): Future[Int] = {
    collection.db.command(Count(collectionName, Some(JsObjectWriter.write(selector))))
  }

  def drop(): Future[Boolean] = {
    collection.drop()
  }

  def dropSync(timeout: Duration = 10 seconds): Boolean = {
    Await.result(drop(), timeout)
  }

  // Iteratee releated APIs

  /** Iteratee.foreach */
  def foreach(selector: JsObject = Json.obj(),
              sort: JsObject = Json.obj("_id" -> 1))(f: (T) => Unit): Future[Unit] = {
    collection.find(selector).sort(sort).cursor[T]
      .enumerate()
      .apply(Iteratee.foreach(f))
      .flatMap(i => i.run)
  }

  /** Iteratee.fold */
  def fold[A](selector: JsObject = Json.obj(),
              sort: JsObject = Json.obj("_id" -> 1),
              state: A)(f: (A, T) => A): Future[A] = {
    collection.find(selector).sort(sort).cursor[T]
      .enumerate()
      .apply(Iteratee.fold(state)(f))
      .flatMap(i => i.run)
  }
}
