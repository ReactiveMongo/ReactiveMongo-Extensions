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
 */
package reactivemongo.extensions.dsl.criteria

import scala.language.implicitConversions

import reactivemongo.bson._

/**
 * The '''ValueBuilder'' type is a model of the ''type class'' pattern used to
 * produce a ''T''-specific [[reactivemongo.bson.BSONValue]] instance.
 *
 * @author svickers
 *
 */
trait ValueBuilder[T] {
  def bson(v: T): BSONValue;
}

/**
 * The '''ValueBuilder''' companion object defines common
 * [[reactivemongo.extensions.dsl.criteria.ValueBuilder]] ''type classes''
 * available for any project.  Types not known to the library can define
 * [[reactivemongo.dsl.ValueBuilder]] instances as needed to extend the DSL.
 */
object ValueBuilder {
  implicit def bsonValueIdentityValue[T <: BSONValue]: ValueBuilder[T] =
    new ValueBuilder[T] {
      override def bson(v: T): T = v;
    }

  implicit object DateTimeValue
      extends ValueBuilder[java.util.Date] {
    override def bson(v: java.util.Date): BSONValue =
      BSONDateTime(v.getTime);
  }

  implicit object BooleanValue
      extends ValueBuilder[Boolean] {
    override def bson(v: Boolean): BSONValue =
      BSONBoolean(v);
  }

  implicit object DoubleValue
      extends ValueBuilder[Double] {
    override def bson(v: Double): BSONValue =
      BSONDouble(v);
  }

  implicit object IntValue
      extends ValueBuilder[Int] {
    override def bson(v: Int): BSONValue =
      BSONInteger(v);
  }

  implicit object LongValue
      extends ValueBuilder[Long] {
    override def bson(v: Long): BSONValue =
      BSONLong(v);
  }

  implicit object StringValue
      extends ValueBuilder[String] {
    override def bson(v: String): BSONValue =
      BSONString(v);
  }

  implicit object SymbolValue
      extends ValueBuilder[Symbol] {
    override def bson(v: Symbol): BSONValue =
      BSONSymbol(v.name);
  }

  implicit object TimestampValue
      extends ValueBuilder[java.sql.Timestamp] {
    override def bson(v: java.sql.Timestamp): BSONValue =
      BSONTimestamp(v.getTime);
  }
}

