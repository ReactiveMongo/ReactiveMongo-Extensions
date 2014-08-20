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

import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.extensions.json.model.DummyModel
import reactivemongo.extensions.dao.MongoContext
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.extensions.util.Misc.UUID

class DummyJsonDao extends {
  override val autoIndexes = Seq(
    Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
    Index(Seq("age" -> IndexType.Ascending), background = true)
  )
} with JsonDao[DummyModel, BSONObjectID](MongoContext.db, "dummy-" + UUID())
