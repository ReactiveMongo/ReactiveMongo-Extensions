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

import org.scalatest._
import org.scalatest.concurrent.{ ScalaFutures }
import org.scalatest.time.{ Span, Seconds }
import play.api.libs.json.{
	OFormat,
	JsError,
	Json,
	JsObject,
	JsResult,
	JsSuccess,
	JsValue
}
import reactivemongo.bson.BSONObjectID
import reactivemongo.extensions.dao.MongoContext
import reactivemongo.extensions.json.dsl.JsonDsl._
import reactivemongo.extensions.util.Logger
import reactivemongo.extensions.Implicits._
import scala.concurrent.{ Future, Await }
import scala.concurrent.ExecutionContext.Implicits.global

class DynamicJsonDaoSpec
		extends FlatSpec
		with Matchers
		with ScalaFutures
		with BeforeAndAfter
		with OneInstancePerTest {

	import play.modules.reactivemongo.json._

	override implicit def patienceConfig = PatienceConfig(timeout = Span(20, Seconds), interval = Span(1, Seconds))

	implicit object JsObjectFormat extends OFormat[JsObject] {
		def reads(json: JsValue): JsResult[JsObject] = json match {
			case obj: JsObject => JsSuccess(obj)
			case _ => JsError("error.jsobject.expected")
		}

		def writes(obj: JsObject): JsObject = obj
	}

	val builder = JsonDaoBuilder[JsObject, BSONObjectID](MongoContext.db)

	before {
		import scala.concurrent.duration._
		Await.ready(builder("collection1").removeAll(), 10 seconds)
		Await.ready(builder("collection2").removeAll(), 10 seconds)
	}

	"A DynamicJsonDao" should "use different collections" in {
		val dao1 = builder("collection1")
		val dao2 = builder("collection2")

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

}
