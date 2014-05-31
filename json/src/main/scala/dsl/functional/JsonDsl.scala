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

package reactivemongo.extensions.json.dsl.functional

import play.api.libs.json._

trait JsonDsl extends reactivemongo.extensions.json.dsl.JsonDsl {

  implicit class ElementBuilder(field: Field) {
    def $eq(value: Value): DefaultElementLike = new DefaultElementLike(field, value)
    def $regex(value: String, options: String): DefaultElementLike = {
      new DefaultElementLike(field, Json.obj("$regex" -> value, "$options" -> options))
    }
    def $exists(exists: Boolean): DefaultElementLike = new DefaultElementLike(field, Json.obj("$exists" -> exists))
    def $ne(value: Value): AppendableElementLike = new AppendableElementLike(field, Json.obj("$ne" -> value))
    def $lt(value: Value): AppendableElementLike = new AppendableElementLike(field, Json.obj("$lt" -> value))
    def $lte(value: Value): AppendableElementLike = new AppendableElementLike(field, Json.obj("$lte" -> value))
    def $gt(value: Value): AppendableElementLike = new AppendableElementLike(field, Json.obj("$gt" -> value))
    def $gte(value: Value): AppendableElementLike = new AppendableElementLike(field, Json.obj("$gte" -> value))
    def $in(value: Value): AppendableElementLike = new AppendableElementLike(field, Json.obj("$in" -> value))
    def $nin(value: Value): AppendableElementLike = new AppendableElementLike(field, Json.obj("$nin" -> value))
  }

  trait ElementLike {
    def toElement: Element
    def toJsObject: JsObject
  }

  class DefaultElementLike(field: Field, value: Value) extends ElementLike {
    def toElement = {
      field -> value
    }
    def toJsObject = {
      Json.obj(field -> value)
    }
  }

  class AppendableElementLike(field: Field, value: JsObject) extends ElementLike {

    def toElement = {
      field -> value
    }

    def toJsObject = {
      Json.obj(field -> value)
    }

    private def append(element: Element): AppendableElementLike = {
      new AppendableElementLike(field, value ++ Json.obj(element))
    }

    def $ne(value: Value): AppendableElementLike = append("$ne" -> value)
    def $lt(value: Value): AppendableElementLike = append("$lt" -> value)
    def $lte(value: Value): AppendableElementLike = append("$lte" -> value)
    def $gt(value: Value): AppendableElementLike = append("$gt" -> value)
    def $gte(value: Value): AppendableElementLike = append("$gte" -> value)
    def $in(value: Value): AppendableElementLike = append("$in" -> value)
    def $nin(value: Value): AppendableElementLike = append("$nin" -> value)
  }

  implicit def toElement(elementLike: ElementLike): Element = {
    elementLike.toElement
  }

  implicit def toJsObject(elementLike: ElementLike): JsObject = {
    elementLike.toJsObject
  }

}

object JsonDsl extends JsonDsl

