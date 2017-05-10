package reactivemongo.extensions

import reactivemongo.bson._
import scala.reflect.runtime.universe.{ TypeTag, typeOf }

object BsonTypes {

	def numberOf[T <: BSONValue: TypeTag]: Int = typeOf[T] match {
		case t if t =:= typeOf[BSONDouble] => 1
		case t if t =:= typeOf[BSONString] => 2
		case t if t =:= typeOf[BSONDocument] => 3
		case t if t =:= typeOf[BSONArray] => 4
		case t if t =:= typeOf[BSONBinary] => 5
		case t if t =:= typeOf[BSONUndefined.type] => 6
		case t if t =:= typeOf[BSONObjectID] => 7
		case t if t =:= typeOf[BSONBoolean] => 8
		case t if t =:= typeOf[BSONDateTime] => 9
		case t if t =:= typeOf[BSONNull.type] => 10
		case t if t =:= typeOf[BSONRegex] => 11
		case t if t =:= typeOf[BSONDBPointer] => 12
		case t if t =:= typeOf[BSONJavaScript] => 13
		case t if t =:= typeOf[BSONSymbol] => 14
		case t if t =:= typeOf[BSONJavaScriptWS] => 15
		case t if t =:= typeOf[BSONInteger] => 16
		case t if t =:= typeOf[BSONTimestamp] => 17
		case t if t =:= typeOf[BSONLong] => 18
		case t if t =:= typeOf[BSONMinKey.type] => 255
		case t if t =:= typeOf[BSONMaxKey.type] => 127
	}
}
