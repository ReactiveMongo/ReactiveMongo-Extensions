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

package reactivemongo.extensions.dsl

import org.scalatest._
import reactivemongo.bson._
import reactivemongo.extensions.util.Logger
import BsonDsl._

class BsonDslSpec extends FlatSpec with Matchers {

  "A BsonDsl" should "create complex document 1" in {
    val dsl: BSONDocument = "age" $gt 50 $lt 60
    Logger.debug(dsl)
    val expected = BSONDocument("age" -> BSONDocument("$gt" -> 50, "$lt" -> 60))
    dsl shouldBe expected
  }

  //**********************************************************************************************//
  // Comparison Operators
  it should "create complex document 2" in {
    val dsl: BSONDocument = "age" $gte 50 $lte 60
    Logger.debug(dsl)
    val expected = BSONDocument("age" -> BSONDocument("$gte" -> 50, "$lte" -> 60))
    dsl shouldBe expected
  }

  it should "create $gt" in {
    val dsl: BSONDocument = "age" $gt 18
    val expected = BSONDocument("age" -> BSONDocument("$gt" -> 18))
    dsl shouldBe expected
  }

  it should "create $gte" in {
    val dsl: BSONDocument = "age" $gte 18
    val expected = BSONDocument("age" -> BSONDocument("$gte" -> 18))
    dsl shouldBe expected
  }

  it should "create $in" in {
    val dsl: BSONDocument = "age" $in (1, 2, 3)
    Logger.debug(dsl)
    val expected = BSONDocument("age" -> BSONDocument("$in" -> BSONArray(1, 2, 3)))
    dsl shouldBe expected
  }

  it should "create $lt" in {
    val dsl: BSONDocument = "age" $lt 18
    val expected = BSONDocument("age" -> BSONDocument("$lt" -> 18))
    dsl shouldBe expected
  }

  it should "create $lte" in {
    val dsl: BSONDocument = "age" $lte 18
    val expected = BSONDocument("age" -> BSONDocument("$lte" -> 18))
    dsl shouldBe expected
  }

  it should "create $ne" in {
    val dsl: BSONDocument = "name" $ne "foo"
    Logger.debug(dsl)
    val expected = BSONDocument("name" -> BSONDocument("$ne" -> "foo"))
    dsl shouldBe expected
  }

  it should "create $nin" in {
    val dsl: BSONDocument = "age" $nin (1, 2, 3)
    Logger.debug(dsl)
    val expected = BSONDocument("age" -> BSONDocument("$nin" -> BSONArray(1, 2, 3)))
    dsl shouldBe expected
  }
  // End of Comparison Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Logical Operators
  it should "create $or" in {
    val dsl = $or("qty" $lt 20 $gte 10, "sale" $eq true)
    Logger.debug(dsl)
    val expected = BSONDocument(
      "$or" -> BSONArray(
        BSONDocument("qty" -> BSONDocument("$lt" -> 20, "$gte" -> 10)),
        BSONDocument("sale" -> true)
      )
    )
    dsl shouldBe expected
  }

  it should "create $and" in {
    val dsl = $and("name" $eq "foo", "surname" $eq "bar", "age" $eq 32)
    Logger.debug(dsl)
    val expected = BSONDocument(
      "$and" -> BSONArray(
        BSONDocument("name" -> "foo"),
        BSONDocument("surname" -> "bar"),
        BSONDocument("age" -> 32)))
    dsl shouldBe expected
  }

  it should "create $not" in {
    val dsl: BSONDocument = "price" $not { _ $gte 5.1 }
    Logger.debug(dsl)
    val expected = BSONDocument("price" -> BSONDocument("$not" -> BSONDocument("$gte" -> 5.1)))
    dsl shouldBe expected
  }

  it should "create $nor" in {
    val dsl = $nor("price" $eq 1.99, "qty" $lt 20, "sale" $eq true)
    Logger.debug(dsl)
    val expected = BSONDocument(
      "$nor" -> BSONArray(
        BSONDocument("price" -> 1.99),
        BSONDocument("qty" -> BSONDocument("$lt" -> 20)),
        BSONDocument("sale" -> true)))
    dsl shouldBe expected
  }
  // End of logical operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Element Operators
  it should "create $exists" in {
    val dsl1: BSONDocument = "qty" $exists true
    val expected1 = BSONDocument("qty" -> BSONDocument("$exists" -> true))
    dsl1 shouldBe expected1

    val dsl2: BSONDocument = "qty" $exists false
    val expected2 = BSONDocument("qty" -> BSONDocument("$exists" -> false))
    dsl2 shouldBe expected2
  }

  it should "create $type" in {
    val dsl1: BSONDocument = "qty".$type[BSONDouble]
    val expected1 = BSONDocument("qty" -> BSONDocument("$type" -> 1))
    dsl1 shouldBe expected1

    val dsl2: BSONDocument = "qty".$type[BSONNull.type]
    val expected2 = BSONDocument("qty" -> BSONDocument("$type" -> 10))
    dsl2 shouldBe expected2
  }
  // End of Element Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Evaluation Operators
  it should "create $mod" in {
    val dsl: BSONDocument = "qty" $mod (5, 0)
    Logger.debug(dsl)
    val expected = BSONDocument("qty" -> BSONDocument("$mod" -> BSONArray(5, 0)))
    dsl shouldBe expected
  }

  it should "create $regex" in {
    val dsl: BSONDocument = "name" $regex ("^Al.*", "i")
    val expected = BSONDocument("name" -> BSONRegex("^Al.*", "i"))
    dsl shouldBe expected
  }

  it should "create $text" in {
    val dsl1: BSONDocument = $text("bake coffee cake")
    val expected1 = BSONDocument("$text" -> BSONDocument("$search" -> "bake coffee cake"))
    dsl1 shouldBe expected1

    val dsl2: BSONDocument = $text("bake coffee cake", "turkish")
    val expected2 = BSONDocument("$text" -> BSONDocument("$search" -> "bake coffee cake", "$language" -> "turkish"))
    dsl2 shouldBe expected2
  }

  it should "create $where" in {
    val dsl: BSONDocument = $where("function () { this.credits == this.debits }")
    val expected = BSONDocument("$where" -> "function () { this.credits == this.debits }")
    dsl shouldBe expected
  }
  // End of Evaluation Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Array Operators
  it should "create $all" in {
    val dsl: BSONDocument = "size" $all ("S", "M", "L")
    val expected = BSONDocument("size" -> BSONDocument("$all" -> BSONArray("S", "M", "L")))
    dsl shouldBe expected
  }

  it should "create $elemMatch" in {
    val dsl: BSONDocument = "array" $elemMatch ("value1" $eq 1, "value2" $gt 1)
    val expected = BSONDocument(
      "array" -> BSONDocument(
        "$elemMatch" -> BSONDocument(
          "value1" -> 1,
          "value2" -> BSONDocument("$gt" -> 1))))
    dsl shouldBe expected
  }

  it should "create $size" in {
    val dsl: BSONDocument = "comments" $size 12
    val expected = BSONDocument("comments" -> BSONDocument("$size" -> 12))
    dsl shouldBe expected
  }
  // End of Array Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Top Level Field Update Operators
  it should "create $inc" in {
    val dsl = $inc("sold" -> 1, "stock" -> -1)
    Logger.debug(dsl)
    val expected = BSONDocument("$inc" -> BSONDocument("sold" -> 1, "stock" -> -1))
    dsl shouldBe expected
  }

  it should "create $mul" in {
    val dsl = $mul("price" -> 1.25)
    Logger.debug(dsl)
    val expected = BSONDocument("$mul" -> BSONDocument("price" -> 1.25))
    dsl shouldBe expected
  }

  it should "create $rename" in {
    val dsl = $rename("color" -> "colour", "realize" -> "realise")
    Logger.debug(dsl)
    val expected = BSONDocument("$rename" -> BSONDocument("color" -> "colour", "realize" -> "realise"))
    dsl shouldBe expected
  }

  it should "create $set" in {
    val dsl = $set("name" -> "foo", "surname" -> "bar", "age" -> 32)

    val expected = BSONDocument(
      "$set" ->
        BSONDocument(
          "name" -> "foo",
          "surname" -> "bar",
          "age" -> 32
        )
    )

    dsl shouldBe expected
  }

  it should "create $unset" in {
    val dsl = $unset("name", "surname", "age")

    val expected = BSONDocument(
      "$unset" ->
        BSONDocument(
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
    val expected = BSONDocument("$addToSet" -> BSONDocument("sizes" -> "L", "colours" -> "Blue"))
    dsl shouldBe expected
  }

  it should "create $pop" in {
    val dsl = $pop("scores" -> -1)
    val expected = BSONDocument("$pop" -> BSONDocument("scores" -> -1))
    dsl shouldBe expected

    the[IllegalArgumentException] thrownBy {
      $pop("scores", 2)
    } should have message "2 is not equal to: -1 | 1"
  }

  it should "create $push" in {
    val dsl = $push("scores", 89)
    val expected = BSONDocument("$push" -> BSONDocument("scores" -> 89))
    dsl shouldBe expected
  }

  it should "create $pushEach" in {
    val dsl = $pushEach("scores", 89, 90, 91, 92)
    val expected = BSONDocument(
      "$push" -> BSONDocument(
        "scores" -> BSONDocument(
          "$each" -> BSONArray(89, 90, 91, 92)
        )
      )
    )

    Logger.debug(dsl)

    dsl shouldBe expected
  }

  it should "create $pull with one value" in {
    val dsl = $pull("flags", "msr")
    val expected = BSONDocument("$pull" -> BSONDocument("flags" -> "msr"))
    dsl shouldBe expected
  }

  it should "create $pull with query" in {
    val dsl = $pull("votes" $gte 6)
    val expected = BSONDocument("$pull" -> BSONDocument("votes" -> BSONDocument("$gte" -> 6)))
    dsl shouldBe expected
  }
  // End ofTop Level Array Update Operators
  //**********************************************************************************************//

}
