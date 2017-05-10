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

package reactivemongo.extensions.json.dao

import play.api.libs.json.{ OFormat, Writes }
import reactivemongo.api.DefaultDB
import reactivemongo.extensions.dao.{ LifeCycle, ReflexiveLifeCycle }

import scala.concurrent.{ ExecutionContext, Future }

class JsonDaoBuilder[Model: OFormat, ID: Writes](db: => Future[DefaultDB]) {
	def apply(collectionName: String)(
		implicit
		lifeCycle: LifeCycle[Model, ID] = new ReflexiveLifeCycle[Model, ID],
		ec: ExecutionContext
	): JsonDao[Model, ID] = {
		JsonDao(db, collectionName)
	}
}

object JsonDaoBuilder {
	def apply[Model: OFormat, ID: Writes](db: => Future[DefaultDB]): JsonDaoBuilder[Model, ID] = {
		new JsonDaoBuilder[Model, ID](db)
	}
}
