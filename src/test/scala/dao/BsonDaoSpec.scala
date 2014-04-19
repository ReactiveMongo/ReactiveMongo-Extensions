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
import reactivemongo.bson.Macros.Options.Verbose
import reactivemongo.extensions.model.DummyModel
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BsonDaoSpec
    extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter
    with ParallelTestExecution {

  override implicit def patienceConfig = PatienceConfig(timeout = 20 seconds, interval = 1 seconds)

  val dao = new DummyBsonDao

  after {
    dao.dropSync()
  }

  "A BsonDao" should "insert bson document" in {
    val dummyModel = DummyModel(name = "foo", surname = "bar", age = 32)

    val futureResult = for {
      insertResult <- dao.insert(dummyModel)
      maybeDummyModel <- dao.findById(dummyModel.id)
      count <- dao.count(Some(BSONDocument("id" -> dummyModel.id)))
    } yield (maybeDummyModel, count)

    whenReady(futureResult) {
      case (maybeDummyModel, count) =>
        maybeDummyModel should be('defined)
        maybeDummyModel.get.id should be(dummyModel.id)
        count should be(1)
    }
  }

  "A BsonDao" should "fold documents" in {
    val dummyModels: Seq[DummyModel] = 1 to 100 map { index =>
      DummyModel(name = s"foo$index", surname = "bar$index", age = index)
    }

    val totalAge = dummyModels.foldLeft(0) { (state, document) => state + document.age }

    val futureResult = for {
      oldTotalAge <- dao.fold(state = 0) { (state, document) => state + document.age }
      insertResult <- Future.sequence(dummyModels.map(dao.insert))
      totalAge <- dao.fold(state = -oldTotalAge) { (state, document) => state + document.age }
    } yield totalAge

    whenReady(futureResult) { result =>
      result shouldBe totalAge
    }
  }

  "A BsonDao" should "iterate(foreach) over documents" in {
    val dummyModels: Seq[DummyModel] = 1 to 100 map { index =>
      DummyModel(name = s"foo$index", surname = "bar$index", age = index)
    }

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

  "A BsonDao" should "set updated field" in {
    val dummyModel = DummyModel(name = "foo", surname = "bar", age = 32)
    val update = BSONDocument("$set" -> BSONDocument("age" -> 64))

    val futureResult = for {
      insert <- dao.insert(dummyModel)
      update <- dao.updateById(dummyModel.id, update)
      updatedMaybeDummyModel <- dao.findById(dummyModel.id)
    } yield updatedMaybeDummyModel

    whenReady(futureResult) { updatedMaybeDummyModel =>
      updatedMaybeDummyModel should be('defined)
      val updatedDummyModel = updatedMaybeDummyModel.get
      updatedDummyModel.id should be(dummyModel.id)
      updatedDummyModel.age should be(64)
      updatedDummyModel.updated.isAfter(dummyModel.updated) should be(true)
    }
  }

}
