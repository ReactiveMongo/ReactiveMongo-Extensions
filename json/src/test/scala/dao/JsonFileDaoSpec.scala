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

package reactivemongo.extensions.dao

import java.io.ByteArrayOutputStream

import org.scalatest._
import org.scalatest.concurrent._
import org.scalatest.time.{ Seconds, Span }
import play.api.libs.iteratee.{ Iteratee, Enumerator }
import play.api.libs.json.{ Json, JsObject }
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global

class JsonFileDaoSpec
    extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter
    with OneInstancePerTest {

  import play.modules.reactivemongo.json._

  override implicit def patienceConfig = PatienceConfig(timeout = Span(20, Seconds), interval = Span(1, Seconds))

  import JsonFileDao._

  val dao = new JsonFileDao[JsObject](MongoContext.db, "json-files") {}

  "A JsonFileDao" should "save and remove file" in {
    val enumerator = Enumerator.fromStream(getClass.getResourceAsStream("/whyfp90.pdf"))

    val result = for {
      save <- dao.save(enumerator, filename = "whyfp90.pdf", contentType = "application/pdf")
      id = BSONFormats.toJSON(save.id).asInstanceOf[JsObject]
      findBefore <- dao.findById(id)
      remove <- dao.removeById(id)
      findAfter <- dao.findById(id)
    } yield (id, findBefore, findAfter)

    whenReady(result) {
      case (id, findBefore, findAfter) =>
        import org.scalatest.OptionValues._
        BSONFormats.toJSON(findBefore.value.id) should be(id)
        findBefore.value.length should be(200007)
        findAfter should be('empty)
    }
  }

  it should "find file by name" in {
    val enumerator = Enumerator.fromStream(getClass.getResourceAsStream("/whyfp90.pdf"))

    val result = for {
      save <- dao.save(enumerator, filename = "whyfp90.pdf", contentType = "application/pdf")
      id = BSONFormats.toJSON(save.id).asInstanceOf[JsObject]
      find <- dao.findOne(Json.obj("filename" -> save.filename))
      remove <- dao.removeById(id)
    } yield (id, find)

    whenReady(result) {
      case (id, find) =>
        import org.scalatest.OptionValues._
        BSONFormats.toJSON(find.value.id) should be(id)
    }
  }

  it should "enumerate one" in {
    val enumerator = Enumerator.fromStream(getClass.getResourceAsStream("/whyfp90.pdf"))

    val length = Iteratee.fold(0) { (state: Int, bytes: Array[Byte]) =>
      state + bytes.size
    }

    val result = for {
      save <- dao.save(enumerator, filename = "whyfp90.pdf", contentType = "application/pdf")
      id = BSONFormats.toJSON(save.id).asInstanceOf[JsObject]
      enumerator <- dao.findOne(Json.obj("filename" -> save.filename)).enumerate
      len <- enumerator.get |>>> length
      remove <- dao.removeById(id)
    } yield (len)

    whenReady(result) { length =>
      length shouldBe 200007
    }
  }

  it should "enumerate by id" in {
    val enumerator = Enumerator.fromStream(getClass.getResourceAsStream("/whyfp90.pdf"))

    val length = Iteratee.fold(0) { (state: Int, bytes: Array[Byte]) =>
      state + bytes.size
    }

    val result = for {
      save <- dao.save(enumerator, filename = "whyfp90.pdf", contentType = "application/pdf")
      id = BSONFormats.toJSON(save.id).asInstanceOf[JsObject]
      enumerator <- dao.findById(id).enumerate
      len <- enumerator.get |>>> length
      remove <- dao.removeById(id)
    } yield (len)

    whenReady(result) { length =>
      length shouldBe 200007
    }
  }

  it should "read one to outputstream" in {
    val enumerator = Enumerator.fromStream(getClass.getResourceAsStream("/whyfp90.pdf"))
    val out = new ByteArrayOutputStream

    val result = for {
      save <- dao.save(enumerator, filename = "whyfp90.pdf", contentType = "application/pdf")
      id = BSONFormats.toJSON(save.id).asInstanceOf[JsObject]
      read <- dao.findOne(Json.obj("filename" -> save.filename)).read(out)
      remove <- dao.removeById(id)
    } yield read

    whenReady(result) { read =>
      read shouldBe ('defined)
      out.size() shouldBe 200007
    }
  }

  it should "read by id to outputstream" in {
    val enumerator = Enumerator.fromStream(getClass.getResourceAsStream("/whyfp90.pdf"))
    val out = new ByteArrayOutputStream

    val result = for {
      save <- dao.save(enumerator, filename = "whyfp90.pdf", contentType = "application/pdf")
      id = BSONFormats.toJSON(save.id).asInstanceOf[JsObject]
      read <- dao.findById(id).read(out)
      remove <- dao.removeById(id)
    } yield read

    whenReady(result) { read =>
      read shouldBe ('defined)
      out.size() shouldBe 200007
    }
  }
}
