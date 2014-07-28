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

import model.DynamicJsonModel
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID
import reactivemongo.extensions.dao.MongoContext
import reactivemongo.extensions.json.dsl.JsonDsl._
import reactivemongo.extensions.Implicits._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class DynamicJsonDaoSpec
    extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter
    with OneInstancePerTest {

  override implicit def patienceConfig = PatienceConfig(timeout = Span(20, Seconds), interval = Span(1, Seconds))

  val builder = JsonDaoBuilder[JsObject, BSONObjectID](MongoContext.db)

  val dao1 = builder("collection1")
  val dao2 = builder("collection2")

  before {
    import scala.concurrent.duration._
    Await.ready(builder("collection1").removeAll(), 10 seconds)
    Await.ready(builder("collection2").removeAll(), 10 seconds)
  }

  "A DynamicJsonDao" should "use different collections" in {
    val futureResult = for {
      insertResult1 <- dao1.insert($doc("name" -> "ali", "surname" -> "veli"))
      insertResult2 <- dao2.insert($doc("name" -> "haydar", "surname" -> "cabbar", "age" -> 18))
      result1 <- ~dao1.findOne("name" $eq "ali")
      result2 <- ~dao2.findOne("name" $eq "haydar")
      insertResult12 <- dao1.insert(result2)
      count1 <- dao1.count()
      count2 <- dao2.count()
    } yield (result1, result2, count1, count2)

    whenReady(futureResult) {
      case (result1, result2, count1, count2) =>
        (result1 \ "surname").as[String] shouldBe "veli"
        (result2 \ "surname").as[String] shouldBe "cabbar"
        count1 shouldBe 2
        count2 shouldBe 1
    }
  }

  it should "find documents by ids" in {
    val dynamicJsonModels = DynamicJsonModel.random(100)

    val futureResult = for {
      insertResult1 <- dao1.bulkInsert(dynamicJsonModels)
      insertResult2 <- dao2.bulkInsert(dynamicJsonModels)
      models1 <- dao1.findByIds(dynamicJsonModels.drop(5).map(doc => (doc \ "_id").as[BSONObjectID]): _*)
      models2 <- dao2.findByIds(dynamicJsonModels.drop(10).map(doc => (doc \ "_id").as[BSONObjectID]): _*)
    } yield (models1, models2)

    whenReady(futureResult) { collection =>
      collection._1 should have size 95
      collection._2 should have size 90
    }
  }

  it should "findAndUpdate one document and retrieve the old document" in {
    val dynamicJsonModel1 = DynamicJsonModel.one
    val dynamicJsonModel2 = DynamicJsonModel.one

    val futureResult = for {
      insertResult1 <- dao1.insert(dynamicJsonModel1)
      oldDocument1 <- ~dao1.findAndUpdate("age" $eq (dynamicJsonModel1 \ "age").as[Int], $inc("age" -> 32))
      newDocument1 <- ~dao1.findOne("age" $eq 33)
      insertResult2 <- dao2.insert(dynamicJsonModel2)
      oldDocument2 <- ~dao2.findAndUpdate("age" $eq (dynamicJsonModel2 \ "age").as[Int], $inc("age" -> 42))
      newDocument2 <- ~dao2.findOne("age" $eq 43)
    } yield (oldDocument1, newDocument1, oldDocument2, newDocument2)

    whenReady(futureResult) {
      case (oldDocument1, newDocument1, oldDocument2, newDocument2) =>
        (oldDocument1 \ "_id").as[BSONObjectID] shouldBe (dynamicJsonModel1 \ "_id").as[BSONObjectID]
        (oldDocument2 \ "_id").as[BSONObjectID] shouldBe (dynamicJsonModel2 \ "_id").as[BSONObjectID]
        (oldDocument1 \ "age").as[Int] shouldBe (dynamicJsonModel1 \ "age").as[Int]
        (oldDocument2 \ "age").as[Int] shouldBe (dynamicJsonModel2 \ "age").as[Int]
        (newDocument1 \ "age").as[Int] shouldBe 33
        (newDocument2 \ "age").as[Int] shouldBe 43
    }
  }

  it should "findAndUpdate one document and retrieve the new document" in {
    val dynamicJsonModel1 = DynamicJsonModel.one
    val dynamicJsonModel2 = DynamicJsonModel.one

    val futureResult = for {
      insertResult1 <- dao1.insert(dynamicJsonModel1)
      newDocument1 <- ~dao1.findAndUpdate("age" $eq (dynamicJsonModel1 \ "age").as[Int], $inc("age" -> 32), fetchNewObject = true)
      insertResult2 <- dao2.insert(dynamicJsonModel2)
      newDocument2 <- ~dao2.findAndUpdate("age" $eq (dynamicJsonModel2 \ "age").as[Int], $inc("age" -> 42), fetchNewObject = true)
    } yield (newDocument1, newDocument2)

    whenReady(futureResult) { case (newDocument1, newDocument2) =>
      (newDocument1 \ "_id").as[BSONObjectID] shouldBe (dynamicJsonModel1 \ "_id").as[BSONObjectID]
      (newDocument2 \ "_id").as[BSONObjectID] shouldBe (dynamicJsonModel2 \ "_id").as[BSONObjectID]
      (newDocument1 \ "age").as[Int] shouldBe 33
      (newDocument2 \ "age").as[Int] shouldBe 43
    }
  }
}
