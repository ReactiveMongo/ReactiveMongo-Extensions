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

package reactivemongo.extensions.json.fixtures

import scala.concurrent.{ ExecutionContext, Future }
import reactivemongo.extensions.util.Logger
import reactivemongo.extensions.fixtures.Fixtures
import reactivemongo.api.DefaultDB
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import collection.JSONCollection
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{ JsObject, Json }

class JsonFixtures(db: => Future[DefaultDB])(implicit ec: ExecutionContext) extends Fixtures[JsObject] {

	def map(document: JsObject): JsObject = document

	def bulkInsert(collectionName: String, documents: Stream[JsObject]): Future[Int] = db.flatMap(_.collection[JSONCollection](
		collectionName
	).bulkInsert(documents, ordered = true).map(_.n))

	def removeAll(collectionName: String): Future[WriteResult] =
		db.flatMap(_.collection[JSONCollection](collectionName).
			remove(selector = Json.obj(), firstMatchOnly = false))

	def drop(collectionName: String): Future[Unit] =
		db.flatMap(_.collection[JSONCollection](collectionName).drop())

}

object JsonFixtures {
	def apply(db: Future[DefaultDB])(implicit ec: ExecutionContext): JsonFixtures = new JsonFixtures(db)
}

