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

package reactivemongo.extensions.json.dsl

import org.scalatest._
import reactivemongo.extensions.util.Logger
import reactivemongo.bson._
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._
import JsonDsl._

class JsonDslSpec extends FlatSpec with Matchers {

  "A JsonDsl" should "create document" in {
    val dsl = $doc("name" $eq "foo", "surname" $eq "bar", "age" $eq 32)

    val expected = Json.obj(
      "name" -> "foo",
      "surname" -> "bar",
      "age" -> 32
    )

    dsl shouldBe expected
  }

  it should "create id document" in {
    val id = BSONObjectID.generate
    val dsl = $id(id)
    val expected = Json.obj("_id" -> id)
    dsl shouldBe expected
  }

  //**********************************************************************************************//
  // Comparison Operators
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
    val dsl: JsObject = "age" $in (1, 2, 3)
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

  it should "create $ne" in {
    val dsl: JsObject = "name" $ne "foo"
    Logger.debug(s"$dsl")
    val expected = Json.obj("name" -> Json.obj("$ne" -> "foo"))
    dsl shouldBe expected
  }

  it should "create $nin" in {
    val dsl: JsObject = "age" $nin (1, 2, 3)
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$nin" -> Json.arr(1, 2, 3)))
    dsl shouldBe expected
  }
  // End of Comparison Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Logical Operators
  it should "create $or" in {
    val dsl = $or("qty" $lt 20 $gte 10, "sale" $eq true)
    Logger.debug(s"$dsl")
    val expected = Json.obj(
      "$or" -> Json.arr(
        Json.obj("qty" -> Json.obj("$lt" -> 20, "$gte" -> 10)),
        Json.obj("sale" -> true)
      )
    )
    dsl shouldBe expected
  }

  it should "create $and" in {
    val dsl = $and("qty" $lt 20 $gte 10, "sale" $eq true)
    Logger.debug(s"$dsl")
    val expected = Json.obj(
      "$and" -> Json.arr(
        Json.obj("qty" -> Json.obj("$lt" -> 20, "$gte" -> 10)),
        Json.obj("sale" -> true)
      )
    )
    dsl shouldBe expected
  }

  it should "create $not" in {
    val dsl: JsObject = "price" $not { _ $gte 5.1 }
    Logger.debug(s"$dsl")
    val expected = Json.obj("price" -> Json.obj("$not" -> Json.obj("$gte" -> 5.1)))
    dsl shouldBe expected
  }

  it should "create $nor" in {
    val dsl = $nor("price" $eq 1.99, "qty" $lt 20, "sale" $eq true)
    Logger.debug(s"$dsl")
    val expected = Json.obj(
      "$nor" -> Json.arr(
        Json.obj("price" -> 1.99),
        Json.obj("qty" -> Json.obj("$lt" -> 20)),
        Json.obj("sale" -> true)))
    dsl shouldBe expected
  }
  // End of logical operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Element Operators
  it should "create $exists" in {
    val dsl1: JsObject = "qty" $exists true
    val expected1 = Json.obj("qty" -> Json.obj("$exists" -> true))
    dsl1 shouldBe expected1

    val dsl2: JsObject = "qty" $exists false
    val expected2 = Json.obj("qty" -> Json.obj("$exists" -> false))
    dsl2 shouldBe expected2
  }

  it should "create $type" in {
    val dsl1: JsObject = "qty".$type[BSONDouble]
    val expected1 = Json.obj("qty" -> Json.obj("$type" -> 1))
    dsl1 shouldBe expected1

    val dsl2: JsObject = "qty".$type[BSONNull.type]
    val expected2 = Json.obj("qty" -> Json.obj("$type" -> 10))
    dsl2 shouldBe expected2
  }
  // End of Element Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Evaluation Operators
  it should "create $mod" in {
    val dsl: JsObject = "qty" $mod (5, 0)
    Logger.debug(s"$dsl")
    val expected = Json.obj("qty" -> Json.obj("$mod" -> Json.arr(5, 0)))
    dsl shouldBe expected
  }

  it should "create $regex" in {
    val dsl: JsObject = "name" $regex ("^Al.*", "i")
    val expected = Json.obj("name" -> Json.obj("$regex" -> "^Al.*", "$options" -> "i"))
    dsl shouldBe expected
  }

  it should "create $text" in {
    val dsl1: JsObject = $text("bake coffee cake")
    val expected1 = Json.obj("$text" -> Json.obj("$search" -> "bake coffee cake"))
    dsl1 shouldBe expected1

    val dsl2: JsObject = $text("bake coffee cake", "turkish")
    val expected2 = Json.obj("$text" -> Json.obj("$search" -> "bake coffee cake", "$language" -> "turkish"))
    dsl2 shouldBe expected2
  }

  it should "create $where" in {
    val dsl: JsObject = $where("function () { this.credits == this.debits }")
    val expected = Json.obj("$where" -> "function () { this.credits == this.debits }")
    dsl shouldBe expected
  }
  // End of Evaluation Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Array Operators
  it should "create $all" in {
    val dsl: JsObject = "size" $all ("S", "M", "L")
    val expected = Json.obj("size" -> Json.obj("$all" -> Json.arr("S", "M", "L")))
    dsl shouldBe expected
  }

  it should "create $elemMatch" in {
    val dsl: JsObject = "array" $elemMatch ("value1" $eq 1, "value2" $gt 1)
    val expected = Json.obj(
      "array" -> Json.obj(
        "$elemMatch" -> Json.obj(
          "value1" -> 1,
          "value2" -> Json.obj("$gt" -> 1))))
    dsl shouldBe expected
  }

  it should "create $size" in {
    val dsl: JsObject = "comments" $size 12
    val expected = Json.obj("comments" -> Json.obj("$size" -> 12))
    dsl shouldBe expected
  }
  // End of Array Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Top Level Field Update Operators
  it should "create $inc" in {
    val dsl = $inc("sold" -> 1, "stock" -> -1)
    Logger.debug(s"$dsl")
    val expected = Json.obj("$inc" -> Json.obj("sold" -> 1, "stock" -> -1))
    dsl shouldBe expected
  }

  it should "create $mul" in {
    val dsl = $mul("price" -> 1.25)
    Logger.debug(s"$dsl")
    val expected = Json.obj("$mul" -> Json.obj("price" -> 1.25))
    dsl shouldBe expected
  }

  it should "create $rename" in {
    val dsl = $rename("color" -> "colour", "realize" -> "realise")
    Logger.debug(s"$dsl")
    val expected = Json.obj("$rename" -> Json.obj("color" -> "colour", "realize" -> "realise"))
    dsl shouldBe expected
  }

  it should "create $set" in {
    val dsl = $set("name" -> "foo", "surname" -> "bar", "age" -> 32)

    val expected = Json.obj(
      "$set" ->
        Json.obj(
          "name" -> "foo",
          "surname" -> "bar",
          "age" -> 32
        )
    )

    dsl shouldBe expected
  }

  it should "create $unset" in {
    val dsl = $unset("name", "surname", "age")

    val expected = Json.obj(
      "$unset" ->
        Json.obj(
          "name" -> "",
          "surname" -> "",
          "age" -> ""
        )
    )

    dsl shouldBe expected
  }
  // End of Top Level Field Update Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Top Level Array Update Operators
  it should "create $addToSet" in {
    val dsl = $addToSet("sizes" -> "L", "colours" -> "Blue")
    val expected = Json.obj("$addToSet" -> Json.obj("sizes" -> "L", "colours" -> "Blue"))
    dsl shouldBe expected
  }

  it should "create $pop" in {
    val dsl = $pop("scores" -> -1)
    val expected = Json.obj("$pop" -> Json.obj("scores" -> -1))
    dsl shouldBe expected

    the[IllegalArgumentException] thrownBy {
      $pop("scores", 2)
    } should have message "2 is not equal to: -1 | 1"
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

  it should "create $push" in {
    val dsl = $push("scores", 89)
    val expected = Json.obj("$push" -> Json.obj("scores" -> 89))
    dsl shouldBe expected
  }

  it should "create $pushEach" in {
    val dsl = $pushEach("scores", 89, 90, 91, 92)
    val expected = Json.obj(
      "$push" -> Json.obj(
        "scores" -> Json.obj(
          "$each" -> Json.arr(89, 90, 91, 92)
        )
      )
    )
    dsl shouldBe expected
  }
  // End ofTop Level Array Update Operators
  //**********************************************************************************************//

}
