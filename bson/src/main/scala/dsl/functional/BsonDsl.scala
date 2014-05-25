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

package reactivemongo.extensions.dsl.functional

import reactivemongo.bson._

trait BsonDsl extends reactivemongo.bson.BsonDsl {

  implicit class ElementBuilder(field: String) {
    def $eq(value: Producer[BSONValue]): DefaultElementLike = new DefaultElementLike(field, produce(value))
    def $regex(value: String, options: String): DefaultElementLike = new DefaultElementLike(field, BSONRegex(value, options))
    def $exists(exists: Boolean): DefaultElementLike = new DefaultElementLike(field, BSONDocument("$exists" -> produce(exists)))
    def $ne(value: Producer[BSONValue]): AppendableElementLike = new AppendableElementLike(field, BSONDocument("$ne" -> produce(value)))
    def $lt(value: Producer[BSONValue]): AppendableElementLike = new AppendableElementLike(field, BSONDocument("$lt" -> produce(value)))
    def $lte(value: Producer[BSONValue]): AppendableElementLike = new AppendableElementLike(field, BSONDocument("$lte" -> produce(value)))
    def $gt(value: Producer[BSONValue]): AppendableElementLike = new AppendableElementLike(field, BSONDocument("$gt" -> produce(value)))
    def $gte(value: Producer[BSONValue]): AppendableElementLike = new AppendableElementLike(field, BSONDocument("$gte" -> produce(value)))
    def $in(value: Producer[BSONValue]): AppendableElementLike = new AppendableElementLike(field, BSONDocument("$in" -> produce(value)))
    def $nin(value: Producer[BSONValue]): AppendableElementLike = new AppendableElementLike(field, BSONDocument("$nin" -> produce(value)))
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

    def $ne(value: Producer[BSONValue]): AppendableElementLike = append("$ne" -> produce(value))
    def $lt(value: Producer[BSONValue]): AppendableElementLike = append("$lt" -> produce(value))
    def $lte(value: Producer[BSONValue]): AppendableElementLike = append("$lte" -> produce(value))
    def $gt(value: Producer[BSONValue]): AppendableElementLike = append("$gt" -> produce(value))
    def $gte(value: Producer[BSONValue]): AppendableElementLike = append("$gte" -> produce(value))
    def $in(value: Producer[BSONValue]): AppendableElementLike = append("$in" -> produce(value))
    def $nin(value: Producer[BSONValue]): AppendableElementLike = append("$nin" -> produce(value))
  }

  implicit def toElement(elementLike: ElementLike): Producer[BSONElement] = {
    elementLike.toBSONElement
  }

  implicit def toBSONDocument(elementLike: ElementLike): BSONDocument = {
    elementLike.toBSONDocument
  }

}

object BsonDsl extends BsonDsl

