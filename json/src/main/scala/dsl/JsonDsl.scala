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

package reactivemongo.extensions.json.dsl

import play.api.libs.json._
import play.api.libs.json.Json.{ JsValueWrapper, toJsFieldJsValueWrapper }
import reactivemongo.bson.{ BSONValue, BSONDocument }
import reactivemongo.extensions.BsonTypes
import scala.reflect.runtime.universe.TypeTag

trait JsonDsl {

  type Field = String

  type Value = JsValueWrapper

  type Element = (Field, Value)

  //**********************************************************************************************//
  // Helpers
  def $empty: JsObject = Json.obj()

  def $doc(elements: Element*): JsObject = {
    Json.obj(elements: _*)
  }

  def $arr(elements: Value*): JsArray = {
    Json.arr(elements: _*)
  }

  def $id(id: Value): JsObject = {
    Json.obj("_id" -> id)
  }
  // End of Helpers
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Top Level Logical Operators
  def $or(expressions: JsObject*): JsObject = {
    Json.obj("$or" -> expressions)
  }

  def $and(expressions: JsObject*): JsObject = {
    Json.obj("$and" -> expressions)
  }

  def $nor(expressions: JsObject*): JsObject = {
    Json.obj("$nor" -> expressions)
  }
  // End of Top Level Logical Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Top Level Evaluation Operators
  def $text(search: String): JsObject = {
    Json.obj("$text" -> Json.obj("$search" -> search))
  }

  def $text(search: String, language: String): JsObject = {
    Json.obj("$text" -> Json.obj("$search" -> search, "$language" -> language))
  }

  def $where(expression: String): JsObject = {
    Json.obj("$where" -> expression)
  }
  // End of Top Level Evaluation Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Top Level Field Update Operators
  def $inc(element: Element, elements: Element*): JsObject = {
    Json.obj("$inc" -> Json.obj((Seq(element) ++ elements): _*))
  }

  def $mul(element: Element): JsObject = {
    Json.obj("$mul" -> Json.obj(element))
  }

  def $rename(item: (String, String), items: (String, String)*): JsObject = {
    Json.obj("$rename" -> Json.obj((Seq(item) ++ items).map {
      case (field, value) =>
        (field, toJsFieldJsValueWrapper(value))
    }: _*))
  }

  def $set(element: Element, elements: Element*): JsObject = {
    Json.obj("$set" -> Json.obj((Seq(element) ++ elements): _*))
  }

  def $unset(field: String, fields: String*): JsObject = {
    Json.obj("$unset" -> Json.obj((Seq(field) ++ fields).map(_ -> toJsFieldJsValueWrapper("")): _*))
  }
  // End of Top Level Field Update Operators
  //**********************************************************************************************//

  //**********************************************************************************************//
  // Top Level Array Update Operators
  def $addToSet(element: Element, elements: Element*): JsObject = {
    Json.obj("$addToSet" -> Json.obj((Seq(element) ++ elements): _*))
  }

  def $pop(item: (String, Int)): JsObject = {
    if (item._2 != -1 && item._2 != 1)
      throw new IllegalArgumentException(s"${item._2} is not equal to: -1 | 1")

    Json.obj("$pop" -> Json.obj(item._1 -> toJsFieldJsValueWrapper(item._2)))
  }

  def $push(element: Element): JsObject = {
    Json.obj("$push" -> Json.obj(element))
  }

  def $pushEach(field: String, values: Value*): JsObject = {
    Json.obj(
      "$push" -> Json.obj(
        field -> Json.obj(
          "$each" -> Json.arr(values: _*)
        )
      )
    )
  }

  def $pull(element: Element): JsObject = {
    Json.obj("$pull" -> Json.obj(element))
  }
  // End ofTop Level Array Update Operators
  //**********************************************************************************************//

  /**
   * Represents the inital state of the expression which has only the name of the field.
   * It does not know the value of the expression.
   */
  trait ElementBuilder {
    def field: String
    def append(value: JsObject): JsObject = value
  }

  /** Represents the state of an expression which has a field and a value */
  trait Expression[V <: JsValue] extends ElementBuilder {
    def value: V
  }

  /*
  * This type of expressions cannot be cascaded. Examples:
  *
  * {{{
  * "price" $eq 10
  * "price" $ne 1000
  * "size" $in ("S", "M", "L")
  * "size" $nin ("S", "XXL")
  * }}}
  *
  */
  case class SimpleExpression[V <: JsValue](field: String, value: V)
    extends Expression[V]

  /**
   * Expressions of this type can be cascaded. Examples:
   *
   * {{{
   *  "age" $gt 50 $lt 60
   *  "age" $gte 50 $lte 60
   * }}}
   *
   */
  case class CompositeExpression(field: String, value: JsObject)
      extends Expression[JsObject]
      with ComparisonOperators {
    override def append(value: JsObject): JsObject = {
      this.value ++ value
    }
  }

  /** MongoDB comparison operators. */
  trait ComparisonOperators { self: ElementBuilder =>

    def $eq[T](value: T)(implicit writer: Writes[T]): SimpleExpression[JsValue] = {
      SimpleExpression(field, writer.writes(value))
    }

    /** Matches values that are greater than the value specified in the query. */
    def $gt[T](value: T)(implicit writer: Writes[T]): CompositeExpression = {
      CompositeExpression(field, append(Json.obj("$gt" -> value)))
    }

    /** Matches values that are greater than or equal to the value specified in the query. */
    def $gte[T](value: T)(implicit writer: Writes[T]): CompositeExpression = {
      CompositeExpression(field, append(Json.obj("$gte" -> value)))
    }

    /** Matches any of the values that exist in an array specified in the query.*/
    def $in[T](values: T*)(implicit writer: Writes[T]): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$in" -> values))
    }

    /** Matches values that are less than the value specified in the query. */
    def $lt[T](value: T)(implicit writer: Writes[T]): CompositeExpression = {
      CompositeExpression(field, append(Json.obj("$lt" -> value)))
    }

    /** Matches values that are less than or equal to the value specified in the query. */
    def $lte[T](value: T)(implicit writer: Writes[T]): CompositeExpression = {
      CompositeExpression(field, append(Json.obj("$lte" -> value)))
    }

    /** Matches all values that are not equal to the value specified in the query. */
    def $ne[T](value: T)(implicit writer: Writes[T]): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$ne" -> value))
    }

    /** Matches values that do not exist in an array specified to the query. */
    def $nin[T](values: T*)(implicit writer: Writes[T]): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$nin" -> values))
    }

  }

  trait LogicalOperators { self: ElementBuilder =>
    def $not(f: (String => Expression[JsObject])): SimpleExpression[JsObject] = {
      val expression = f(field)
      SimpleExpression(field, Json.obj("$not" -> expression.value))
    }
  }

  trait ElementOperators { self: ElementBuilder =>
    def $exists(exists: Boolean): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$exists" -> exists))
    }

    def $type[T <: BSONValue: TypeTag]: SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$type" -> BsonTypes.numberOf[T]))
    }
  }

  trait EvaluationOperators { self: ElementBuilder =>
    def $mod(divisor: Int, remainder: Int): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$mod" -> Json.arr(divisor, remainder)))
    }

    def $regex(value: String, options: String): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$regex" -> value, "$options" -> options))
    }
  }

  trait ArrayOperators { self: ElementBuilder =>
    def $all[T](values: T*)(implicit writer: Writes[T]): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$all" -> values))
    }

    def $elemMatch(query: (Field, Value)*): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$elemMatch" -> Json.obj(query: _*)))
    }

    def $size(size: Int): SimpleExpression[JsObject] = {
      SimpleExpression(field, Json.obj("$size" -> size))
    }
  }

  implicit class ElementBuilderLike(val field: String)
    extends ElementBuilder
    with ComparisonOperators
    with ElementOperators
    with EvaluationOperators
    with LogicalOperators
    with ArrayOperators

  implicit def toElement[V <: JsValue](expression: Expression[V])(implicit writer: Writes[V]): (Field, Value) = {
    expression.field -> expression.value
  }

  implicit def toJsObject[V <: JsValue](expression: Expression[V])(implicit writer: Writes[V]): JsObject = {
    Json.obj(expression.field -> expression.value)
  }

}

object JsonDsl extends JsonDsl
