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

import reactivemongo.api.{ DefaultDB, MongoDriver }
import reactivemongo.extensions.util.Misc.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object MongoContext {
	val driver = new MongoDriver
	val connection = driver.connection(List("localhost"))
	def db: Future[DefaultDB] = connection.database("test-reactivemongo-extensions")
	def randomDb: Future[DefaultDB] = connection.database(UUID())
	def syncDb = Await.result(connection.database("test-reactivemongo-extensions"), 10.seconds)
}
