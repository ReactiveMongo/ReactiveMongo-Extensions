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

package reactivemongo.extensions.dsl.functional

import org.scalatest._
import reactivemongo.extensions.util.Logger
import reactivemongo.bson.BSONObjectID
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._
import JsonDsl._

class JsonDslSpec extends FlatSpec with Matchers {

  "A JsonDsl" should "create document" in {
    val dsl = $and("name" $eq "foo", "surname" $eq "bar", "age" $eq 32)

    val expected = Json.obj(
      "name" -> "foo",
      "surname" -> "bar",
      "age" -> 32)

    dsl shouldBe expected
  }

  it should "create id document" in {
    val id = BSONObjectID.generate
    val dsl = $id(id)
    val expected = Json.obj("_id" -> id)
    dsl shouldBe expected
  }

  it should "create complex document 1" in {
    val dsl: JsObject = "age" $gt 50 $lt 60
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$gt" -> 50, "$lt" -> 60))
    dsl shouldBe expected
  }

  it should "create complex document 2" in {
    val dsl: JsObject = "age" $gte 50 $lte 60
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$gte" -> 50, "$lte" -> 60))
    dsl shouldBe expected
  }

  it should "create $or" in {
    val dsl = $or("qty" $lt 20 $gte 10, "sale" $eq true)
    Logger.debug(s"$dsl")
    val expected = Json.obj(
      "$or" -> Json.arr(
        Json.obj("qty" -> Json.obj("$lt" -> 20, "$gte" -> 10)),
        Json.obj("sale" -> true)))
    dsl shouldBe expected
  }

  it should "create $ne" in {
    val dsl: JsObject = "name" $ne "foo"
    Logger.debug(s"$dsl")
    val expected = Json.obj("name" -> Json.obj("$ne" -> "foo"))
    dsl shouldBe expected
  }

  it should "create $gt" in {
    val dsl: JsObject = "age" $gt 18
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$gt" -> 18))
    dsl shouldBe expected
  }

  it should "create $gte" in {
    val dsl: JsObject = "age" $gte 18
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$gte" -> 18))
    dsl shouldBe expected
  }

  it should "create $in" in {
    val seq = Seq(1, 2, 3)
    val dsl: JsObject = "age" $in seq
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$in" -> Json.arr(1, 2, 3)))
    dsl shouldBe expected
  }

  it should "create $lt" in {
    val dsl: JsObject = "age" $lt 18
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$lt" -> 18))
    dsl shouldBe expected
  }

  it should "create $lte" in {
    val dsl: JsObject = "age" $lte 18
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$lte" -> 18))
    dsl shouldBe expected
  }

  it should "create $nin" in {
    val seq = Seq(1, 2, 3)
    val dsl: JsObject = "age" $nin seq
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$nin" -> Json.arr(1, 2, 3)))
    dsl shouldBe expected
  }

  it should "create $pull with one value" in {
    val dsl = $pull("flags", "msr")
    val expected = Json.obj("$pull" -> Json.obj("flags" -> "msr"))
    dsl shouldBe expected
  }

  it should "create $pull with query" in {
    val dsl = $pull("votes" $gte 6)
    val expected = Json.obj("$pull" -> Json.obj("votes" -> Json.obj("$gte" -> 6)))
    dsl shouldBe expected
  }

  it should "create $exists" in {
    val dsl1: JsObject = "qty" $exists true
    val expected1 = Json.obj("qty" -> Json.obj("$exists" -> true))
    dsl1 shouldBe expected1

    val dsl2: JsObject = "qty" $exists false
    val expected2 = Json.obj("qty" -> Json.obj("$exists" -> false))
    dsl2 shouldBe expected2
  }

  it should "create $regex" in {
    val dsl: JsObject = "name" $regex ("^Al.*", "i")
    val expected = Json.obj("name" -> Json.obj("$regex" -> "^Al.*", "$options" -> "i"))
    dsl shouldBe expected
  }

}
