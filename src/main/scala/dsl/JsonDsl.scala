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

// In order to use Producer.produce we must be in this package.
package reactivemongo.extensions.dsl

import play.api.libs.json._
import play.api.libs.json.Json.{ JsValueWrapper, toJsFieldJsValueWrapper }
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

object JsonDsl {

  type Field = String

  type Value = JsValueWrapper

  type Element = (Field, Value)

  def $doc(element: Element, elements: Element*): JsObject = {
    Json.obj((Seq(element) ++ elements): _*)
  }

  def $docx(field: Field, element: Element, elements: Element*): JsObject = {
    Json.obj(field -> Json.obj((Seq(element) ++ elements): _*))
  }

  def $id(id: BSONObjectID): JsObject = {
    $doc("_id" -> id)
  }

  def $ne(element: Element): JsObject = {
    Json.obj(element._1 -> Json.obj("$ne" -> element._2))
  }

  def $gt(element: Element): JsObject = {
    Json.obj(element._1 -> Json.obj("$gt" -> element._2))
  }

  def $gtx(value: Value): Element = {
    "$gt" -> value
  }

  def $gtex(value: Value): Element = {
    "$gte" -> value
  }

  def $ltx(value: Value): Element = {
    "$lt" -> value
  }

  def $ltex(value: Value): Element = {
    "$lte" -> value
  }

  def $nin(field: String, values: Value*): JsObject = {
    Json.obj(field -> Json.obj("$nin" -> Json.arr(values: _*)))
  }

  def $set(element: Element, elements: Element*): JsObject = {
    Json.obj("$set" -> Json.obj((Seq(element) ++ elements): _*))
  }

  def $unset(field: String, fields: String*): JsObject = {
    Json.obj("$unset" -> Json.obj((Seq(field) ++ fields).map(_ -> toJsFieldJsValueWrapper("")): _*))
  }

  def $push(element: Element): JsObject = {
    Json.obj("$push" -> Json.obj(element))
  }

  def $pushEach(field: String, values: Value*): JsObject = {
    Json.obj(
      "$push" -> Json.obj(
        field -> Json.obj(
          "$each" -> Json.arr(values: _*))))
  }

  def $pull(element: Element): JsObject = {
    Json.obj("$pull" -> Json.obj(element))
  }

}
