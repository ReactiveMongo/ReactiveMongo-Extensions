package reactivemongo.extensions.dsl

import reactivemongo.bson.{BSONDocument, BSONValue, BSONWriter}

object BsonDsl {

  implicit class BsonField(name: String) {

    def :=[A, B <: BSONValue](value: A)(implicit writer: BSONWriter[A, B]): BsonElement[A, B] = {
      BsonElement(name, value)
    }

    def :=[A, B <: BSONValue](value: Option[A])(implicit writer: BSONWriter[A, B]): Option[BsonElement[A, B]] = {
      value map (name := _)
    }
  }

  implicit class BsonDocument(document: BSONDocument) {
    def ~(element: BsonElement[_, _]): BSONDocument = {
      document ++ element.toDoc
    }
  }

}
