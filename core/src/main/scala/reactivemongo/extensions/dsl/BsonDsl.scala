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

  object $addToSet extends BsonField("$addToSet")

  object $all extends BsonField("$all")

  object $and extends BsonField("$and")

  object $elemMatch extends BsonField("$elemMatch")

  object $exists extends BsonField("$exists")

  object $gt extends BsonField("$gt")

  object $gte extends BsonField("$gte")

  object $in extends BsonField("$in")

  object $inc extends BsonField("$inc")

  object $lt extends BsonField("$lt")

  object $lte extends BsonField("$lte")

  object $max extends BsonField("$max")

  object $min extends BsonField("$min")

  object $mod extends BsonField("$mod")

  object $mul extends BsonField("$mul")

  object $ne extends BsonField("$ne")

  object $nin extends BsonField("$nin")

  object $not extends BsonField("$not")

  object $or extends BsonField("$or")

  object $pop extends BsonField("$pop")

  object $pull extends BsonField("$pull")

  object $push extends BsonField("$push")

  object $pushEach extends BsonField("$pushEach")

  object $regex extends BsonField("$regex")

  object $rename extends BsonField("$rename")

  object $search extends BsonField("$search")

  object $set extends BsonField("$set")

  object $setOnInsert extends BsonField("$setOnInsert")

  object $size extends BsonField("$size")

  object $text extends BsonField("$text")

  object $type extends BsonField("$type")

  object $unset extends BsonField("$unset")

  object $where extends BsonField("$where")

}
