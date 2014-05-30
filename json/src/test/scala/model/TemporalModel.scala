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

package reactivemongo.extensions.json.model

import reactivemongo.bson._
import reactivemongo.extensions.dao.Handlers._
import reactivemongo.extensions.dao.LifeCycle
import reactivemongo.extensions.util.Logger
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._
import org.joda.time.DateTime

case class TemporalModel(
  _id: BSONObjectID = BSONObjectID.generate,
  name: String,
  surname: String,
  createdAt: DateTime = DateTime.now,
  updatedAt: DateTime = DateTime.now)

object TemporalModel {
  implicit val temporalModelFormat = Json.format[TemporalModel]

  implicit object TemporalModelLifeCycle extends LifeCycle[TemporalModel, BSONObjectID] {
    def prePersist(model: TemporalModel): TemporalModel = {
      Logger.debug(s"prePersist $model")
      model.copy(updatedAt = DateTime.now)
    }

    def postPersist(model: TemporalModel): Unit = {
      Logger.debug(s"postPersist $model")
    }

    def preRemove(id: BSONObjectID): Unit = {
      Logger.debug(s"preRemove $id")
    }

    def postRemove(id: BSONObjectID): Unit = {
      Logger.debug(s"postRemove $id")
    }

    def ensuredIndexes(): Unit = {
      Logger.debug("ensuredIndexes")
    }
  }
}
