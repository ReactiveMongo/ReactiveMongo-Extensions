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
package reactivemongo.bson

import reactivemongo.bson._

object BsonDsl {

  implicit def bsonDocumentToPretty(document: BSONDocument): String = {
    BSONDocument.pretty(document)
  }

  def $doc(item: Producer[BSONElement], items: Producer[BSONElement]*): BSONDocument = {
    BSONDocument((Seq(item) ++ items): _*)
  }

  def $doc(key: String, element: BSONElement, elements: BSONElement*): BSONDocument = {
    BSONDocument(key -> BSONDocument((Seq(element) ++ elements)))
  }

  def $id(id: BSONObjectID): BSONDocument = {
    BSONDocument("_id" -> id)
  }

  def $ne(item: Producer[BSONElement]): BSONDocument = {
    val (key, value) = item.produce.get
    BSONDocument(key -> BSONDocument("$ne" -> value))
  }

  def $gt(item: Producer[BSONElement]): BSONDocument = {
    val (key, value) = item.produce.get
    BSONDocument(key -> BSONDocument("$gt" -> value))
  }

  def $gt(value: Producer[BSONValue]): BSONElement = {
    "$gt" -> value.produce.get
  }

  def $in(key: String, values: Producer[BSONValue]*): BSONDocument = {
    BSONDocument(key -> BSONDocument("$in" -> BSONArray(values.map(_.produce.get))))
  }

  def $gte(item: Producer[BSONElement]): BSONDocument = {
    val (key, value) = item.produce.get
    BSONDocument(key -> BSONDocument("$gte" -> value))
  }

  def $gte(value: Producer[BSONValue]): BSONElement = {
    "$gte" -> value.produce.get
  }

  def $lt(item: Producer[BSONElement]): BSONDocument = {
    val (key, value) = item.produce.get
    BSONDocument(key -> BSONDocument("$lt" -> value))
  }

  def $lt(value: Producer[BSONValue]): BSONElement = {
    "$lt" -> value.produce.get
  }

  def $lte(item: Producer[BSONElement]): BSONDocument = {
    val (key, value) = item.produce.get
    BSONDocument(key -> BSONDocument("$lte" -> value))
  }

  def $lte(value: Producer[BSONValue]): BSONElement = {
    "$lte" -> value.produce.get
  }

  def $nin(key: String, values: Producer[BSONValue]*): BSONDocument = {
    BSONDocument(key -> BSONDocument("$nin" -> BSONArray(values.map(_.produce.get))))
  }

  def $set(item: Producer[BSONElement], items: Producer[BSONElement]*): BSONDocument = {
    BSONDocument("$set" -> BSONDocument((Seq(item) ++ items): _*))
  }

  def $unset(key: String, keys: String*): BSONDocument = {
    BSONDocument("$unset" -> BSONDocument((Seq(key) ++ keys).map(_ -> BSONString(""))))
  }
}
