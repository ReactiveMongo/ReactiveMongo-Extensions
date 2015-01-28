package reactivemongo.extensions.dsl

import reactivemongo.bson.{BSONDocument, BSONValue, BSONWriter}

case class BsonElement[A, B <: BSONValue](name: String, value: A)(implicit writer: BSONWriter[A, B]) {

  def toDoc: BSONDocument = BSONDocument(name -> value)

  def ~(that: BsonElement[_, _]): BSONDocument = this.toDoc ++ that.toDoc

  def ~(that: Option[BsonElement[_, _]]): BSONDocument = that match {
    case Some(that) => this ~ that
    case None => this.toDoc
  }
}
