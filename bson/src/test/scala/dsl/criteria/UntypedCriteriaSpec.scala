/*
 * Copyright 2013 Steve Vickers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Created on: Jun 2, 2013
 */
package reactivemongo.extensions.dsl.criteria

import org.scalatest._
import org.scalatest.matchers._

import reactivemongo.bson._

/** The '''UntypedCriteriaSpec''' type unit tests the
 *  [[reactivemongo.extensions.dsl.criteria.Untyped]] EDSL functionality and
 *  serves both to verify fitness as well as an exemplar to how the
 *  [[reactivemongo.extensions.dsl.criteria.Untyped]] functionality can be used.
 *
 *  @author svickers
 *
 */
class UntypedCriteriaSpec
		extends FlatSpec
		with Matchers {
	/// Class Imports
	import Untyped._

	"An Untyped criteria" should "support equality comparisons" in
		{
			BSONDocument.pretty(criteria.myField === "a value") shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"myField" -> BSONString("a value")
					)
				)
			);

			BSONDocument.pretty(criteria.myField @== "a value") shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"myField" -> BSONString("a value")
					)
				)
			);
		}

	it should "support inequality comparisons" in
		{
			BSONDocument.pretty(criteria.myField !== "a value") shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"myField" ->
							BSONDocument("$ne" -> BSONString("a value"))
					)
				)
			);

			BSONDocument.pretty(criteria.myField =/= "a value") shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"myField" ->
							BSONDocument("$ne" -> BSONString("a value"))
					)
				)
			);

			BSONDocument.pretty(criteria.myField <> "a value") shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"myField" ->
							BSONDocument("$ne" -> BSONString("a value"))
					)
				)
			);
		}

	it should "support nested object selectors" in
		{
			val q = criteria.outer.inner =/= 99;

			BSONDocument.pretty(q) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"outer.inner" ->
							BSONDocument("$ne" -> BSONInteger(99))
					)
				)
			);
		}

	it should "support String operations" in
		{
			val q = criteria.str =~ "^test|re";

			BSONDocument.pretty(q) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"str" ->
							BSONDocument(
								"$regex" -> BSONRegex("^test|re", "")
							)
					)
				)
			);
		}

	it should "support conjunctions" in
		{
			val q = criteria.first < 10 && criteria.second >= 20.0;

			BSONDocument.pretty(BSONDocument(q.element)) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$and" ->
							BSONArray(
								BSONDocument(
									"first" -> BSONDocument("$lt" -> BSONInteger(10))
								),
								BSONDocument(
									"second" -> BSONDocument("$gte" -> BSONDouble(20.0))
								)
							)
					)
				)
			);
		}

	it should "support disjunctions" in
		{
			val q = criteria.first < 10 || criteria.second >= 20.0;

			BSONDocument.pretty(BSONDocument(q.element)) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$or" ->
							BSONArray(
								BSONDocument(
									"first" -> BSONDocument("$lt" -> BSONInteger(10))
								),
								BSONDocument(
									"second" -> BSONDocument("$gte" -> BSONDouble(20.0))
								)
							)
					)
				)
			);
		}

	it should "combine adjacent conjunctions" in
		{
			val q = criteria.first < 10 && criteria.second >= 20.0 && criteria.third < 0.0;

			BSONDocument.pretty(BSONDocument(q.element)) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$and" ->
							BSONArray(
								BSONDocument(
									"first" -> BSONDocument("$lt" -> BSONInteger(10))
								),
								BSONDocument(
									"second" -> BSONDocument("$gte" -> BSONDouble(20.0))
								),
								BSONDocument(
									"third" -> BSONDocument("$lt" -> BSONDouble(0.0))
								)
							)
					)
				)
			);
		}

	it should "combine adjacent disjunctions" in
		{
			val q = criteria.first < 10 || criteria.second >= 20.0 || criteria.third < 0.0;

			BSONDocument.pretty(BSONDocument(q.element)) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$or" ->
							BSONArray(
								BSONDocument(
									"first" -> BSONDocument("$lt" -> BSONInteger(10))
								),
								BSONDocument(
									"second" -> BSONDocument("$gte" -> BSONDouble(20.0))
								),
								BSONDocument(
									"third" -> BSONDocument("$lt" -> BSONDouble(0.0))
								)
							)
					)
				)
			);
		}

	it should "support compound filtering" in
		{
			val q = criteria.first < 10 && (criteria.second >= 20.0 || criteria.second.in(0.0, 1.0));

			BSONDocument.pretty(q) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$and" ->
							BSONArray(
								BSONDocument(
									"first" -> BSONDocument("$lt" -> BSONInteger(10))
								),
								BSONDocument(
									"$or" ->
										BSONArray(
											BSONDocument(
												"second" -> BSONDocument("$gte" -> BSONDouble(20.0))
											),
											BSONDocument(
												"second" ->
													BSONDocument(
														"$in" ->
															BSONArray(
																BSONDouble(0.0),
																BSONDouble(1.0)
															)
													)
											)
										)
								)
							)
					)
				)
			);
		}

	it should "support alternating logical operators" in
		{
			val q = criteria.first < 10 && criteria.second >= 20.0 || criteria.third < 0.0 && criteria.fourth =~ "some regex";

			BSONDocument.pretty(BSONDocument(q.element)) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$or" ->
							BSONArray(
								BSONDocument(
									"$and" ->
										BSONArray(
											BSONDocument(
												"first" -> BSONDocument("$lt" -> BSONInteger(10))
											),
											BSONDocument(
												"second" -> BSONDocument("$gte" -> BSONDouble(20.0))
											)
										)
								),
								BSONDocument(
									"$and" ->
										BSONArray(
											BSONDocument(
												"third" -> BSONDocument("$lt" -> BSONDouble(0.0))
											),
											BSONDocument(
												"fourth" -> BSONDocument("$regex" -> BSONRegex("some regex", ""))
											)
										)
								)
							)
					)
				)
			);
		}

	it should "support logical negation" in
		{
			BSONDocument.pretty(!(criteria.a === 42)) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"a" ->
							BSONDocument("$ne" -> BSONInteger(42))
					)
				)
			);

			BSONDocument.pretty(!(criteria.a =~ "regex(p)?")) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$not" ->
							BSONDocument(
								"a" ->
									BSONDocument("$regex" -> BSONRegex("regex(p)?", ""))
							)
					)
				)
			);

			BSONDocument.pretty(!(criteria.xyz === 1 || criteria.xyz === 2)) shouldBe (
				BSONDocument.pretty(
					BSONDocument(
						"$nor" ->
							BSONArray(
								BSONDocument("xyz" -> BSONInteger(1)),
								BSONDocument("xyz" -> BSONInteger(2))
							)
					)
				)
			);
		}

	it should "have an 'empty' resulting in no criteria" in
		{
			BSONDocument.pretty(Expression.empty) shouldBe (
				BSONDocument.pretty(BSONDocument())
			);
		}

	it should "ignore 'empty' in logical operators" in
		{
			BSONDocument.pretty(criteria.a === 1 && Expression.empty) shouldBe (
				BSONDocument.pretty(
					BSONDocument("a" -> BSONInteger(1))
				)
			);

			BSONDocument.pretty(Expression.empty && criteria.a === 2.0) shouldBe (
				BSONDocument.pretty(
					BSONDocument("a" -> BSONDouble(2.0))
				)
			);

			BSONDocument.pretty(Expression.empty || criteria.a === "three") shouldBe (
				BSONDocument.pretty(
					BSONDocument("a" -> BSONString("three"))
				)
			);

			BSONDocument.pretty(criteria.a === 4L || Expression.empty) shouldBe (
				BSONDocument.pretty(
					BSONDocument("a" -> BSONLong(4L))
				)
			);
		}
}

