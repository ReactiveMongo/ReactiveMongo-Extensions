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

  "A BsonDsl" should "create document" in {
    val dsl = $doc("name" -> "foo", "surname" -> "bar", "age" -> 32)

    val expected = BSONDocument(
      "name" -> "foo",
      "surname" -> "bar",
      "age" -> 32
    )

    dsl shouldBe expected
  }

  it should "create complex document 1" in {
    val dsl = $docx("age", $gtx(50), $ltx(60))
    Logger.debug(dsl)
    val expected = BSONDocument("age" -> BSONDocument("$gt" -> 50, "$lt" -> 60))
    dsl shouldBe expected
  }

  it should "create complex document 2" in {
    val dsl = $docx("age", $gtex(50), $ltex(60))
    Logger.debug(dsl)
    val expected = BSONDocument("age" -> BSONDocument("$gte" -> 50, "$lte" -> 60))
    dsl shouldBe expected
  }

  it should "create $or" in {
    val dsl = $or($lt("qty" -> 20), $doc("sale" -> true))
    Logger.debug(dsl)
    val expected = BSONDocument(
      "$or" -> BSONArray(
        BSONDocument("qty" -> BSONDocument("$lt" -> 20)),
        BSONDocument("sale" -> true)
      )
    )
    dsl shouldBe expected
  }

  it should "create $ne" in {
    val dsl = $ne("name" -> "foo")
    Logger.debug(dsl)
    val expected = BSONDocument("name" -> BSONDocument("$ne" -> "foo"))
    dsl shouldBe expected
  }

  it should "create $gt" in {
    val dsl = $gt("name" -> "foo")
    val expected = BSONDocument("name" -> BSONDocument("$gt" -> "foo"))
    dsl shouldBe expected
  }

  it should "create $gte" in {
    val dsl = $gte("name" -> "foo")
    val expected = BSONDocument("name" -> BSONDocument("$gte" -> "foo"))
    dsl shouldBe expected
  }

  it should "create $in" in {
    val seq = Seq(1, 2, 3)
    val dsl = $in("age", seq)
    Logger.debug(dsl)
    val expected = BSONDocument("age" -> BSONDocument("$in" -> BSONArray(1, 2, 3)))
    dsl shouldBe expected
  }

  it should "create $lt" in {
    val dsl = $lt("name" -> "foo")
    val expected = BSONDocument("name" -> BSONDocument("$lt" -> "foo"))
    dsl shouldBe expected
  }

  it should "create $lte" in {
    val dsl = $lte("name" -> "foo")
    val expected = BSONDocument("name" -> BSONDocument("$lte" -> "foo"))
    dsl shouldBe expected
  }

  it should "create $nin" in {
    val seq = Seq(1, 2, 3)
    val dsl = $nin("age", seq)
    Logger.debug(dsl)
    val expected = BSONDocument("age" -> BSONDocument("$nin" -> BSONArray(1, 2, 3)))
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
    val dsl = $pull("votes", $doc($gtex(6)))
    val expected = BSONDocument("$pull" -> BSONDocument("votes" -> BSONDocument("$gte" -> 6)))
    dsl shouldBe expected
  }

  it should "create $exists" in {
    val dsl1 = $exists("qty")
    val expected1 = BSONDocument("qty" -> BSONDocument("$exists" -> true))
    dsl1 shouldBe expected1

    val dsl2 = $exists("qty", false)
    val expected2 = BSONDocument("qty" -> BSONDocument("$exists" -> false))
    dsl2 shouldBe expected2
  }

  it should "create $regex" in {
    val dsl = $regex("name", "^Al.*", "i")
    val expected = BSONDocument("name" -> BSONRegex("^Al.*", "i"))
    dsl shouldBe expected
  }

}
