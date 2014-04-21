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

package reactivemongo.extensions.dsl

import org.scalatest._
import reactivemongo.extensions.util.Logger
import reactivemongo.bson.BSONObjectID
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._
import JsonDsl._

class JsonDslSpec extends FlatSpec with Matchers {

  "A JsonDsl" should "create document" in {
    val dsl = $doc("name" -> "foo", "surname" -> "bar", "age" -> 32)

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
    val dsl = $docex("age", $gt(50), $lt(60))
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$gt" -> 50, "$lt" -> 60))
    dsl shouldBe expected
  }

  it should "create complex document 2" in {
    val dsl = $docex("age", $gte(50), $lte(60))
    Logger.debug(s"$dsl")
    val expected = Json.obj("age" -> Json.obj("$gte" -> 50, "$lte" -> 60))
    dsl shouldBe expected
  }
}
