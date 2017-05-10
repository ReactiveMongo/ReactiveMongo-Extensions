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

import play.api.libs.json.{ JsObject, JsValue, Json, Writes }
import reactivemongo.api.{ DB, DBMetaCommands, DefaultDB }
import reactivemongo.api.gridfs.IdProducer
import reactivemongo.bson.BSONValue
import reactivemongo.extensions.dao.FileDao.ReadFileWrapper

import scala.concurrent.{ ExecutionContext, Future }

/** {{{
 *  import reactivemongo.extensions.dao.JsonFileDao, JsonFileDao._
 *  }}}
 */
abstract class JsonFileDao[Id <: JsValue: IdProducer](db: => DB with DBMetaCommands, collectionName: String)(implicit gridFsId: Id => BSONValue)
		extends FileDao[Id, JsObject](db, collectionName) {

	import play.modules.reactivemongo.json.JsObjectWriter

	def findById(id: Id)(implicit ec: ExecutionContext): ReadFileWrapper =
		findOne(Json.obj("_id" -> id))
}

object JsonFileDao {
	import reactivemongo.play.json.BSONFormats

	// !! unsafe
	implicit def defaultGridFSBSONId[T <: JsValue](json: T): BSONValue =
		BSONFormats.toBSON(json).getOrElse(sys.error(s"fails to convert $json"))
}
