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

package reactivemongo.extensions.dsl.functional

import reactivemongo.bson._

trait BsonDsl extends reactivemongo.extensions.dsl.BsonDsl {

  implicit class ElementBuilder(field: String) {

    def $eq[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): DefaultElementLike = {
      new DefaultElementLike(field, writer.write(value))
    }

    def $regex(value: String, options: String): DefaultElementLike = {
      new DefaultElementLike(field, BSONRegex(value, options))
    }

    def $exists(exists: Boolean): DefaultElementLike = {
      new DefaultElementLike(field, BSONDocument("$exists" -> exists))
    }

    def $ne[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      new AppendableElementLike(field, BSONDocument("$ne" -> value))
    }

    def $lt[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      new AppendableElementLike(field, BSONDocument("$lt" -> value))
    }

    def $lte[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      new AppendableElementLike(field, BSONDocument("$lte" -> value))
    }

    def $gt[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      new AppendableElementLike(field, BSONDocument("$gt" -> value))
    }

    def $gte[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      new AppendableElementLike(field, BSONDocument("$gte" -> value))
    }

    def $in[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      new AppendableElementLike(field, BSONDocument("$in" -> value))
    }

    def $nin[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      new AppendableElementLike(field, BSONDocument("$nin" -> value))
    }
  }

  trait ElementLike {
    def toBSONElement: BSONElement
    def toBSONDocument: BSONDocument
  }

  class DefaultElementLike(field: String, value: BSONValue) extends ElementLike {
    def toBSONElement = {
      field -> value
    }
    def toBSONDocument = {
      BSONDocument(field -> value)
    }
  }

  class AppendableElementLike(field: String, value: BSONDocument) extends ElementLike {

    def toBSONElement = {
      field -> value
    }

    def toBSONDocument = {
      BSONDocument(field -> value)
    }

    private def append(element: BSONElement): AppendableElementLike = {
      new AppendableElementLike(field, value ++ BSONDocument(element))
    }

    def $ne[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      append("$ne" -> writer.write(value))
    }

    def $lt[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      append("$lt" -> writer.write(value))
    }

    def $lte[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      append("$lte" -> writer.write(value))
    }

    def $gt[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      append("$gt" -> writer.write(value))
    }

    def $gte[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      append("$gte" -> writer.write(value))
    }

    def $in[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      append("$in" -> writer.write(value))
    }

    def $nin[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): AppendableElementLike = {
      append("$nin" -> writer.write(value))
    }
  }

  implicit def toElement(elementLike: ElementLike): Producer[BSONElement] = {
    elementLike.toBSONElement
  }

  implicit def toBSONDocument(elementLike: ElementLike): BSONDocument = {
    elementLike.toBSONDocument
  }

}

object BsonDsl extends BsonDsl

