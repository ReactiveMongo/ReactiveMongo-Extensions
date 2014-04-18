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
import reactivemongo.bson._
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.{ LastError, Count }
import reactivemongo.extensions.model.Model

abstract class BsonDao[T <: Model: BSONDocumentReader: BSONDocumentWriter] extends Dao[BSONCollection] {

  def findById(id: String): Future[Option[T]] = {
    findOne(BSONDocument("id" -> id))
  }

  def findOne(query: BSONDocument): Future[Option[T]] = {
    collection.find(query).one[T]
  }

  def insert(document: BSONDocument): Future[LastError] = {
    collection.insert(document)
  }

  def insert(document: T): Future[LastError] = {
    collection.insert(document)
  }

  def updateById(id: String, query: BSONDocument): Future[LastError] = {
    collection.update(BSONDocument("id" -> id), query)
  }

  def count(query: Option[BSONDocument] = None): Future[Int] = {
    collection.db.command(Count(collectionName, query))
  }
}

