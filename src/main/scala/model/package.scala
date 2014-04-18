package reactivemongo.extensions

import org.joda.time.DateTime
import reactivemongo.bson._

package object model {

  implicit object BSONDateTimeHandler
      extends BSONReader[BSONDateTime, DateTime]
      with BSONWriter[DateTime, BSONDateTime] {

    def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)

    def write(date: DateTime) = BSONDateTime(date.getMillis)
  }

}
