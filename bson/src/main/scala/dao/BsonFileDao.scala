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

import reactivemongo.bson.{ BSONWriter, BSONDocument, BSONValue, Producer }
import reactivemongo.api.{ DBMetaCommands, DB }
import reactivemongo.api.gridfs.IdProducer
import reactivemongo.extensions.dao.FileDao.ReadFileWrapper
import scala.concurrent.ExecutionContext

abstract class BsonFileDao[Id <: BSONValue: IdProducer](db: => DB with DBMetaCommands, collectionName: String) extends FileDao[Id, BSONDocument](db, collectionName) {

	def findById(id: Id)(implicit ec: ExecutionContext): ReadFileWrapper = {
		findOne(BSONDocument("_id" -> id))
	}
}
