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

import play.api.libs.json.{ Json, JsObject }
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

  def $docex(field: Field, element: Element, elements: Element*): JsObject = {
    Json.obj(field -> Json.obj((Seq(element) ++ elements): _*))
  }

  def $id(id: BSONObjectID): JsObject = {
    $doc("_id" -> id)
  }

  def $gt(value: Value): Element = {
    "$gt" -> value
  }

  def $gte(value: Value): Element = {
    "$gte" -> value
  }

  def $lt(value: Value): Element = {
    "$lt" -> value
  }

  def $lte(value: Value): Element = {
    "$lte" -> value
  }

}
