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

import reactivemongo.api.DB
import reactivemongo.bson.{
  BSONDocumentWriter,
  BSONDocumentReader,
  BSONReader,
  BSONValue,
  BSONWriter
}

import scala.concurrent.ExecutionContext

class BsonDaoBuilder[Model, ID](db: => DB) {
  def apply(collectionName: String)(
    implicit modelReader: BSONDocumentReader[Model],
    modelWriter: BSONDocumentWriter[Model],
    idWriter: BSONWriter[ID, _ <: BSONValue],
    idReader: BSONReader[_ <: BSONValue, ID],
    lifeCycle: LifeCycle[Model, ID] = new ReflexiveLifeCycle[Model, ID],
    ec: ExecutionContext): BsonDao[Model, ID] = {
    BsonDao(db, collectionName)
  }
}

object BsonDaoBuilder {
  def apply[Model, ID](db: => DB): BsonDaoBuilder[Model, ID] = {
    new BsonDaoBuilder[Model, ID](db)
  }
}
