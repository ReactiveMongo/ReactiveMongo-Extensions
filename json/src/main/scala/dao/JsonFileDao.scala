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

import play.api.libs.json.{ JsObject, Json, Writes }
import play.modules.reactivemongo.json.ImplicitBSONHandlers.JsObjectWriter
import reactivemongo.api.{ DB, DBMetaCommands }
import reactivemongo.bson.BSONValue
import reactivemongo.extensions.dao.FileDao.ReadFileWrapper

import scala.concurrent.{ ExecutionContext, Future }

abstract class JsonFileDao[Id <: BSONValue: Writes](db: => DB with DBMetaCommands, collectionName: String)
    extends FileDao[Id, JsObject](db, collectionName) {

  def findById(id: Id)(implicit ec: ExecutionContext): ReadFileWrapper = {
    findOne(Json.obj("_id" -> id))
  }
}
