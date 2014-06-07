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

package reactivemongo.extensions.dsl

import reactivemongo.bson._

trait BsonDsl {

  implicit def bsonDocumentToPretty(document: BSONDocument): String = {
    BSONDocument.pretty(document)
  }

  def $doc(item: Producer[BSONElement], items: Producer[BSONElement]*): BSONDocument = {
    BSONDocument((Seq(item) ++ items): _*)
  }

  def $and(item: Producer[BSONElement], items: Producer[BSONElement]*): BSONDocument = {
    $doc(item, items: _*)
  }

  def $docx(field: String, element: BSONElement, elements: BSONElement*): BSONDocument = {
    BSONDocument(field -> BSONDocument((Seq(element) ++ elements)))
  }

  def $id[T](id: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    BSONDocument("_id" -> id)
  }

  def $exists(field: String, exists: Boolean = true): BSONDocument = {
    $doc(field -> $doc("$exists" -> exists))
  }

  def $ne[T](item: (String, T))(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    val (field, value) = item
    BSONDocument(field -> BSONDocument("$ne" -> value))
  }

  def $gt[T](item: (String, T))(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    val (field, value) = item
    BSONDocument(field -> BSONDocument("$gt" -> value))
  }

  def $gtx[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONElement = {
    "$gt" -> writer.write(value)
  }

  def $gte[T](item: (String, T))(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    val (field, value) = item
    BSONDocument(field -> BSONDocument("$gte" -> value))
  }

  def $gtex[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONElement = {
    "$gte" -> writer.write(value)
  }

  def $in[T <: TraversableOnce[_]](field: String, values: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    BSONDocument(field -> BSONDocument("$in" -> values))
  }

  def $lt[T](item: (String, T))(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    val (field, value) = item
    BSONDocument(field -> BSONDocument("$lt" -> value))
  }

  def $ltx[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONElement = {
    "$lt" -> writer.write(value)
  }

  def $lte[T](item: (String, T))(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    val (field, value) = item
    BSONDocument(field -> BSONDocument("$lte" -> value))
  }

  def $ltex[T](value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONElement = {
    "$lte" -> writer.write(value)
  }

  def $nin[T <: TraversableOnce[_]](field: String, values: T)(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    BSONDocument(field -> BSONDocument("$nin" -> values))
  }

  def $set(item: Producer[BSONElement], items: Producer[BSONElement]*): BSONDocument = {
    BSONDocument("$set" -> BSONDocument((Seq(item) ++ items): _*))
  }

  def $unset(field: String, fields: String*): BSONDocument = {
    BSONDocument("$unset" -> BSONDocument((Seq(field) ++ fields).map(_ -> BSONString(""))))
  }

  def $push(item: Producer[BSONElement]): BSONDocument = {
    BSONDocument("$push" -> BSONDocument(item))
  }

  def $pushEach[T](field: String, values: T*)(implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocument = {
    BSONDocument(
      "$push" -> BSONDocument(
        field -> BSONDocument(
          "$each" -> values
        )
      )
    )
  }

  def $pull(item: Producer[BSONElement]): BSONDocument = {
    BSONDocument("$pull" -> BSONDocument(item))
  }

  def $or(expressions: BSONDocument*): BSONDocument = {
    BSONDocument("$or" -> expressions)
  }

  def $regex(field: String, value: String, options: String) = {
    BSONDocument(field -> BSONRegex(value, options))
  }

}

object BsonDsl extends BsonDsl

