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

import org.scalatest._
import org.scalatest.concurrent._
import org.scalatest.time.SpanSugar._
import reactivemongo.bson._
import reactivemongo.bson.Macros.Options.Verbose
import reactivemongo.extensions.model.TemporalModel
import reactivemongo.extensions.dsl.BsonDsl._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TemporalModelDaoSpec
    extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter
    with OneInstancePerTest {

  override implicit def patienceConfig = PatienceConfig(timeout = 20 seconds, interval = 1 seconds)

  val dao = new TemporalModelDao

  after {
    dao.dropSync()
  }

  "A TemporalModelDao" should "update updateAt" in {
    val temporalModel = TemporalModel(name = "foo", surname = "bar")

    val futureResult = for {
      insertResult <- dao.insert(temporalModel)
      maybeTemporalModel <- dao.findById(temporalModel._id)
    } yield maybeTemporalModel

    whenReady(futureResult) { maybeTemporalModel =>
      maybeTemporalModel should be('defined)
      maybeTemporalModel.get._id shouldBe temporalModel._id
      maybeTemporalModel.get.name shouldBe temporalModel.name
      maybeTemporalModel.get.surname shouldBe temporalModel.surname
      maybeTemporalModel.get.createdAt shouldBe temporalModel.createdAt
      maybeTemporalModel.get.updatedAt.isAfter(temporalModel.updatedAt) shouldBe true
    }
  }

}
