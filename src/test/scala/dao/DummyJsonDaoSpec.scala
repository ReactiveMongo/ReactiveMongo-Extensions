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

package reactivemongo.extensions.dao

import org.scalatest._
import org.scalatest.concurrent._
import org.scalatest.time.SpanSugar._
import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.extensions.model.DummyModel
import reactivemongo.extensions.dsl.functional.JsonDsl._
import reactivemongo.extensions.util.Logger
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DummyJsonDaoSpec
    extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter
    with OneInstancePerTest {

  override implicit def patienceConfig = PatienceConfig(timeout = 20 seconds, interval = 1 seconds)

  val dao = new DummyJsonDao

  after {
    dao.dropSync()
  }

  "A DummyJsonDao" should "find one document" in {
    val dummyModel = DummyModel(name = "foo", surname = "bar", age = 32)

    val futureResult = for {
      insertResult <- dao.insert(dummyModel)
      maybeDummyModel <- dao.findOne("age" $eq dummyModel.age)
    } yield maybeDummyModel

    whenReady(futureResult) { maybeDummyModel =>
      maybeDummyModel should be('defined)
      maybeDummyModel.get._id shouldBe dummyModel._id
      maybeDummyModel.get.age shouldBe dummyModel.age
    }
  }

  it should "find one random document in selected documents" in {
    val dummyModels = DummyModel.random(100)

    val futureResult = for {
      insertCount <- dao.insert(dummyModels)
      random <- dao.findRandom("age" $gt 50 $lt 60)
    } yield random

    whenReady(futureResult) { random =>
      random should be('defined)
      random.get.age should be > 50
      random.get.age should be < 60
    }
  }

  it should "find all of the selected documents" in {
    val dummyModels = DummyModel.random(100)

    val futureModels = for {
      insertResult <- dao.insert(dummyModels)
      models <- dao.findAll("age" $gte 50)
    } yield models

    whenReady(futureModels) { models =>
      models should have size 51
    }
  }

  it should "find all documents" in {
    val dummyModels = DummyModel.random(100)

    val futureModels = for {
      insertResult <- dao.insert(dummyModels)
      models <- dao.findAll()
    } yield models

    whenReady(futureModels) { models =>
      models should have size 100
    }
  }

  it should "find one random document in all documents" in {
    val dummyModels = DummyModel.random(100)

    val futureResult = for {
      insertCount <- dao.insert(dummyModels)
      random <- dao.findRandom()
    } yield random

    whenReady(futureResult) { random =>
      Logger.trace(s"$random")
      random should be('defined)
      random.get.age should be >= 1
      random.get.age should be <= 100
    }
  }

  it should "find documents by ids" in {
    val dummyModels = DummyModel.random(100)

    val futureResult = for {
      insertCount <- dao.insert(dummyModels)
      models <- dao.findByIds(dummyModels.map(_._id))
    } yield models

    whenReady(futureResult) { models =>
      models should have size 100
    }
  }

  it should "insert bson document" in {
    val dummyModel = DummyModel(name = "foo", surname = "bar", age = 32)

    val futureResult = for {
      insertResult <- dao.insert(dummyModel)
      maybeDummyModel <- dao.findById(dummyModel._id)
      count <- dao.count($id(dummyModel._id))
    } yield (maybeDummyModel, count)

    whenReady(futureResult) {
      case (maybeDummyModel, count) =>
        maybeDummyModel should be('defined)
        maybeDummyModel.get._id shouldBe dummyModel._id
        count shouldBe 1
    }
  }

  it should "insert multiple documents" in {
    val dummyModels = DummyModel.random(100)

    val futureResult = for {
      insertCount <- dao.insert(dummyModels)
      count <- dao.count()
    } yield (insertCount, count)

    whenReady(futureResult) {
      case (insertCount, count) =>
        insertCount shouldBe 100
        count shouldBe 100
    }
  }

  it should "count selected documents" in {
    val dummyModels = DummyModel.random(100)

    val futureCount = for {
      insertResult <- dao.insert(dummyModels)
      count <- dao.count("age" $gte 50)
    } yield count

    whenReady(futureCount) { count =>
      count shouldBe 51
    }
  }

  it should "count all documents" in {
    val dummyModels = DummyModel.random(100)

    val futureCount = for {
      insertResult <- dao.insert(dummyModels)
      count <- dao.count()
    } yield count

    whenReady(futureCount) { count =>
      count shouldBe 100
    }
  }

  it should "fold documents" in {
    val dummyModels = DummyModel.random(100)
    val totalAge = dummyModels.foldLeft(0) { (state, document) => state + document.age }

    val futureResult = for {
      oldTotalAge <- dao.fold(state = 0) { (state, document) => state + document.age }
      insertResult <- dao.insert(dummyModels)
      totalAge <- dao.fold(state = -oldTotalAge) { (state, document) => state + document.age }
    } yield totalAge

    whenReady(futureResult) { result =>
      result shouldBe totalAge
    }
  }

  it should "iterate(foreach) over documents" in {
    val dummyModels = DummyModel.random(100)
    val totalAge = dummyModels.foldLeft(0) { (state, document) => state + document.age }

    val futureResult = for {
      oldTotalAge <- dao.fold(state = 0) { (state, document) => state + document.age }
      insertResult <- dao.insert(dummyModels)
      totalAge <- {
        var total = -oldTotalAge // Just for the test case, please don't do this
        dao.foreach()(total += _.age).map(_ => total)
      }
    } yield totalAge

    whenReady(futureResult) { result =>
      result shouldBe totalAge
    }
  }

  it should "save document" in {
    val dummyModel = DummyModel(name = "foo", surname = "bar", age = 32)

    val futureResult = for {
      insert <- dao.save(dummyModel)
      maybeInsertedDummyModel <- dao.findById(dummyModel._id)
      update <- dao.save(dummyModel.copy(age = 64))
      maybeUpdatedDummyModel <- dao.findById(dummyModel._id)
    } yield (maybeInsertedDummyModel, maybeUpdatedDummyModel)

    whenReady(futureResult) {
      case (maybeInsertedDummyModel, maybeUpdatedDummyModel) =>
        maybeInsertedDummyModel should be('defined)
        val insertedDummyModel = maybeInsertedDummyModel.get
        insertedDummyModel._id shouldBe dummyModel._id
        insertedDummyModel.age shouldBe 32

        maybeUpdatedDummyModel should be('defined)
        val updatedDummyModel = maybeUpdatedDummyModel.get
        updatedDummyModel._id shouldBe dummyModel._id
        updatedDummyModel.age shouldBe 64
    }
  }

  it should "ensure indexes" in {
    val futureIndexes = Future {
      // Give some time for indexes to be ensured
      Thread.sleep(2000)
    } flatMap { _ =>
      dao.listIndexes()
    }

    whenReady(futureIndexes) { indexes =>
      indexes should have size 3 // including _id
    }
  }

  it should "remove document by id" in {
    val dummyModels = DummyModel.random(10)

    val futureResult = for {
      insertCount <- dao.insert(dummyModels)
      beforeCount <- dao.count()
      remove <- dao.removeById(dummyModels.head._id)
      afterCount <- dao.count()
    } yield (insertCount, beforeCount, afterCount)

    whenReady(futureResult) {
      case (insertCount, beforeCount, afterCount) =>
        insertCount shouldBe 10
        beforeCount shouldBe 10
        afterCount shouldBe 9
    }
  }

  it should "remove all documents" in {
    val dummyModels = DummyModel.random(10)

    val futureResult = for {
      insertCount <- dao.insert(dummyModels)
      beforeCount <- dao.count()
      remove <- dao.removeAll()
      afterCount <- dao.count()
    } yield (insertCount, beforeCount, afterCount)

    whenReady(futureResult) {
      case (insertCount, beforeCount, afterCount) =>
        insertCount shouldBe 10
        beforeCount shouldBe 10
        afterCount shouldBe 0
    }
  }

}
