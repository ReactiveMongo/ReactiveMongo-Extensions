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
import reactivemongo.extensions.dsl.functional.BsonDsl._
import reactivemongo.extensions.model.MapModel
import reactivemongo.extensions.util.Logger
import reactivemongo.extensions.Implicits._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MapModelBsonDaoSpec
    extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter
    with OneInstancePerTest {

  override implicit def patienceConfig = PatienceConfig(timeout = 20 seconds, interval = 1 seconds)

  val dao = new MapModelBsonDao

  after {
    dao.dropSync()
  }

  "A MapModelBsonDao" should "find one document" in {
    val mapModel = MapModel(data = Map("count" -> 1))

    val futureResult = for {
      insertResult <- dao.insert(mapModel)
      maybeMapModel <- dao.findOne()
    } yield maybeMapModel

    whenReady(futureResult) { maybeMapModel =>
      maybeMapModel should be('defined)
      maybeMapModel.get._id shouldBe mapModel._id
      maybeMapModel.get.data("count") shouldBe 1
    }
  }

  it should "support ~ operator" in {
    val mapModel = MapModel(data = Map("count" -> 1))

    val futureResult = for {
      insertResult <- dao.insert(mapModel)
      mapModel <- ~dao.findOne()
      count <- dao.count($id(mapModel._id))
    } yield (mapModel, count)

    whenReady(futureResult) {
      case (foundMapModel, count) =>
        foundMapModel._id shouldBe mapModel._id
        foundMapModel.data("count") shouldBe 1
        count shouldBe 1
    }
  }

  it should "throw exception when using ~ operator with None" in {
    val mapModel = MapModel(data = Map("count" -> 1))

    val futureResult = (for {
      insertResult <- dao.insert(mapModel)
      mapModel <- ~dao.findOne("none" $eq "unknown")
      count <- dao.count($id(mapModel._id))
    } yield count) recover {
      case ex: java.util.NoSuchElementException => ex
    }

    whenReady(futureResult) { ex =>
      ex shouldBe a[java.util.NoSuchElementException]
    }
  }

  it should "save document" in {
    val mapModel = MapModel(data = Map("count" -> 1))

    val futureResult = for {
      insert <- dao.save(mapModel)
      maybeInsertedDummyModel <- dao.findById(mapModel._id)
      newData = mapModel.data + ("total" -> 2, "count" -> 2)
      update <- dao.save(mapModel.copy(data = newData))
      maybeUpdatedDummyModel <- dao.findById(mapModel._id)
    } yield (maybeInsertedDummyModel, maybeUpdatedDummyModel)

    whenReady(futureResult) {
      case (maybeInsertedDummyModel, maybeUpdatedDummyModel) =>
        maybeInsertedDummyModel should be('defined)
        val insertedDummyModel = maybeInsertedDummyModel.get
        insertedDummyModel._id shouldBe mapModel._id
        insertedDummyModel.data("count") shouldBe 1

        maybeUpdatedDummyModel should be('defined)
        val updatedDummyModel = maybeUpdatedDummyModel.get
        updatedDummyModel._id shouldBe mapModel._id
        updatedDummyModel.data("count") shouldBe 2
        updatedDummyModel.data("total") shouldBe 2
    }
  }

}
