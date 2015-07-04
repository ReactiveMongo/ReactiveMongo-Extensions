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

import org.joda.time.DateTime
import reactivemongo.bson._

object Handlers {

  implicit object BSONDateTimeHandler
      extends BSONReader[BSONDateTime, DateTime]
      with BSONWriter[DateTime, BSONDateTime] {

    def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)

    def write(date: DateTime) = BSONDateTime(date.getMillis)
  }

  implicit def MapBSONReader[T](implicit reader: BSONReader[_ <: BSONValue, T]): BSONDocumentReader[Map[String, T]] =
    new BSONDocumentReader[Map[String, T]] {
      def read(doc: BSONDocument): Map[String, T] = {
        doc.elements.collect {
          case (key, value) => value.seeAsOpt[T](reader) map {
            ov => (key, ov)
          }
        }.flatten.toMap
      }
    }

  implicit def MapBSONWriter[T](implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocumentWriter[Map[String, T]] = new BSONDocumentWriter[Map[String, T]] {
    def write(doc: Map[String, T]): BSONDocument = {
      BSONDocument(doc.toTraversable map (t => (t._1, writer.write(t._2))))
    }
  }

  implicit def MapReader[V](implicit vr: BSONDocumentReader[V]): BSONDocumentReader[Map[String, V]] = new BSONDocumentReader[Map[String, V]] {
    def read(bson: BSONDocument): Map[String, V] = {
      val elements = bson.elements.map { tuple =>
        // assume that all values in the document are BSONDocuments
        tuple._1 -> vr.read(tuple._2.seeAsTry[BSONDocument].get)
      }
      elements.toMap
    }
  }

  implicit def MapWriter[V](implicit vw: BSONDocumentWriter[V]): BSONDocumentWriter[Map[String, V]] = new BSONDocumentWriter[Map[String, V]] {
    def write(map: Map[String, V]): BSONDocument = {
      val elements = map.toStream.map { tuple =>
        tuple._1 -> vw.write(tuple._2)
      }
      BSONDocument(elements)
    }
  }

  implicit object BSONIntegerHandler extends BSONReader[BSONValue, Int] {
    def read(bson: BSONValue) = bson.asOpt[BSONNumberLike] match {
      case Some(num) => num.toInt
      case _ => bson match {
        case doc @ BSONDocument(_) =>
          doc.getAs[BSONNumberLike]("$int").map(_.toInt).get
      }
    }
  }

  implicit object BSONLongHandler extends BSONReader[BSONValue, Long] {
    def read(bson: BSONValue) = bson.asOpt[BSONNumberLike] match {
      case Some(num) => num.toLong
      case _ => bson match {
        case doc @ BSONDocument(_) =>
          doc.getAs[BSONNumberLike]("$long").map(_.toLong).get
      }
    }
  }

  implicit object BSONDoubleHandler extends BSONReader[BSONValue, Double] {
    def read(bson: BSONValue) = bson.asOpt[BSONNumberLike] match {
      case Some(num) => num.toDouble
      case _ => bson match {
        case doc @ BSONDocument(_) =>
          doc.getAs[BSONNumberLike]("$double").map(_.toDouble).get
      }
    }
  }
}
