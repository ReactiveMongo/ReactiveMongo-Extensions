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

package reactivemongo.bson

import org.scalatest._
import reactivemongo.extensions.util.Logger
import BsonDsl._

class BsonDslSpec extends FlatSpec with Matchers {

  "A BsonDsl" should "create $eq" in {
    val dsl = $eq("name" -> "foo", "surname" -> "bar", "age" -> 32)

    val expected = BSONDocument(
      "name" -> "foo",
      "surname" -> "bar",
      "age" -> 32)

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
    val dsl = $in("age", 1, 2, 3)
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
    val dsl = $nin("age", 1, 2, 3)
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
          "age" -> 32))

    dsl shouldBe expected
  }

}
