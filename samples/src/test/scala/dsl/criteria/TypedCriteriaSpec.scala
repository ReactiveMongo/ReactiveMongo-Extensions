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
 * Created on: Jun 22, 2014
 */
package reactivemongo.extensions.samples.dsl.criteria

import org.scalatest._
import org.scalatest.junit.JUnitRunner

import reactivemongo.bson._
import reactivemongo.extensions.dsl.criteria._

trait Company {
	val name: String;
	val employees: Int;
}

case class Person(val firstName: String, val lastName: String, age: Int)

/** The '''TypedCriteriaSpec''' type verifies the expected behaviour of the
 *  [[reactivemongo.extensions.dsl.criteria.Typed]] Criteria DSL.
 *
 *  @author svickers
 *
 */
class TypedCriteriaSpec
		extends FlatSpec
		with Matchers {
	/// Class Imports
	import Typed._

	"Typed criteria" should "produce a Term" in
		{
			val t = criteria[Person].firstName;

			t.getClass shouldBe (classOf[Term[_]]);
		}

	it should "produce a Term with the given property name" in
		{
			val t = criteria[Person].lastName;

			t match {
				case Term(propertyName) =>
					propertyName shouldBe ("lastName");
			}
		}

	it should "work with valid expressions" in
		{
			(criteria[Person].firstName =~ "sample regex") shouldBe (
				Expression(
					Some("firstName"),
					("$regex", BSONRegex("sample regex", ""))
				)
			);
		}

	it should "support trait-based expressions" in
		{
			val query = criteria[Company].employees === 42 ||
				criteria[Company].employees < 10;

			BSONDocument.pretty(query) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$or" ->
							BSONArray(
								BSONDocument(
									"employees" -> BSONInteger(42)
								),
								BSONDocument(
									"employees" ->
										BSONDocument("$lt" -> 10)
								)
							)
					)
				)
			);
		}
}

