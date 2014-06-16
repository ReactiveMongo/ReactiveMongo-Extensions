/*
 * Copyright 2013 Steve Vickers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on: Jun 15, 2014
 */
package reactivemongo.extensions.dsl.criteria

import org.scalatest._
import org.scalatest.matchers._

import reactivemongo.bson._

/**
 * The '''UntypedWhereSpec''' type verifies the behaviour expected of the
 * `where` method in the [[reactivemongo.extensions.dsl.criteria.Untyped]]
 * `type`.
 *
 * @author svickers
 *
 */
class UntypedWhereSpec
    extends FlatSpec
    with Matchers {
  /// Class Imports
  import Untyped._

  "An Untyped where" should "support 1 placeholder" in
    {
      val q = where {
        _.a === 1
      }

      BSONDocument.pretty(q) shouldBe (
        BSONDocument.pretty(
          BSONDocument(
            "a" -> BSONInteger(1)
          )
        )
      );
    }

  it should "support 2 placeholders" in
    {
      val q = where {
        _.a === 1 && _.b === 2
      }

      BSONDocument.pretty(q) shouldBe (
        BSONDocument.pretty(
          BSONDocument(
            "$and" ->
              BSONArray(
                BSONDocument(
                  "a" -> BSONInteger(1)
                ),
                BSONDocument(
                  "b" -> BSONInteger(2)
                )
              )
          )
        )
      );
    }

  it should "support 3 placeholders" in
    {
      val q = where {
        _.a === 1 && _.b === 2 && _.c === 3
      }

      BSONDocument.pretty(q) shouldBe (
        BSONDocument.pretty(
          BSONDocument(
            "$and" ->
              BSONArray(
                BSONDocument(
                  "a" -> BSONInteger(1)
                ),
                BSONDocument(
                  "b" -> BSONInteger(2)
                ),
                BSONDocument(
                  "c" -> BSONInteger(3)
                )
              )
          )
        )
      );
    }

  /// The library supports from 1 to 22 placeholders for the where method.
  it should "support 22 placeholders" in
    {
      val q = where {
        _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0 &&
          _.p === 0
      }

      BSONDocument.pretty(q) shouldBe (
        BSONDocument.pretty(
          BSONDocument(
            "$and" ->
              BSONArray(List.fill(22)(BSONDocument("p" -> BSONInteger(0))))
          )
        )
      );
    }
}
