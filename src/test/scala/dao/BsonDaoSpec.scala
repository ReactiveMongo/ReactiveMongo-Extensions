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
import reactivemongo.bson._
import reactivemongo.bson.BsonDsl._
import reactivemongo.bson.Macros.Options.Verbose
import reactivemongo.extensions.model.DummyModel
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BsonDaoSpec
    extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter
    with OneInstancePerTest {

  override implicit def patienceConfig = PatienceConfig(timeout = 20 seconds, interval = 1 seconds)

  val dao = new DummyBsonDao

  after {
    dao.dropSync()
  }

  "A BsonDao" should "find one document" in {
    val dummyModel = DummyModel(name = "foo", surname = "bar", age = 32)

    val futureResult = for {
      insertResult <- dao.insert(dummyModel)
      maybeDummyModel <- dao.findOne($doc("age" -> dummyModel.age))
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
      random <- dao.findRandom($docex("age", $gt(50), $lt(60)))
    } yield random

    whenReady(futureResult) { random =>
      random should be('defined)
      random.get.age should be > 50
      random.get.age should be < 60
    }
  }

  it should "find one random document in all documents" in {
    val dummyModels = DummyModel.random(100)

    val futureResult = for {
      insertCount <- dao.insert(dummyModels)
      random <- dao.findRandom()
    } yield random

    whenReady(futureResult) { random =>
      random should be('defined)
      random.get.age should be > 1
      random.get.age should be <= 100
    }
  }

  it should "find selected documents" in {
    val dummyModels = DummyModel.random(100)

    val futureResult = for {
      insertCount <- dao.insert(dummyModels)
      selectedModels <- dao.find(page = 2, pageSize = 20, sort = $doc("age" -> 1))
    } yield selectedModels

    whenReady(futureResult) { selectedModels =>
      selectedModels should have size 20
      selectedModels.head.age shouldBe 21
      selectedModels.last.age shouldBe 40
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
      insertResult <- Future.sequence(dummyModels.map(dao.insert))
      count <- dao.count($gte("age" -> 50))
    } yield count

    whenReady(futureCount) { count =>
      count shouldBe 51
    }
  }

  it should "count all documents" in {
    val dummyModels = DummyModel.random(100)

    val futureCount = for {
      insertResult <- Future.sequence(dummyModels.map(dao.insert))
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
      insertResult <- Future.sequence(dummyModels.map(dao.insert))
      totalAge <- {
        var total = -oldTotalAge // Just for the test case, please don't do this
        dao.foreach()(total += _.age).map(_ => total)
      }
    } yield totalAge

    whenReady(futureResult) { result =>
      result shouldBe totalAge
    }
  }

  it should "set update document by id" in {
    val dummyModel = DummyModel(name = "foo", surname = "bar", age = 32)
    val update = $set("age" -> 64)

    val futureResult = for {
      insert <- dao.insert(dummyModel)
      update <- dao.updateById(dummyModel._id, update)
      updatedMaybeDummyModel <- dao.findById(dummyModel._id)
    } yield updatedMaybeDummyModel

    whenReady(futureResult) { updatedMaybeDummyModel =>
      updatedMaybeDummyModel should be('defined)
      val updatedDummyModel = updatedMaybeDummyModel.get
      updatedDummyModel._id shouldBe dummyModel._id
      updatedDummyModel.age shouldBe 64
    }
  }

}
