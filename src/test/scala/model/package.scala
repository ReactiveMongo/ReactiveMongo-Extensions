package reactivemongo.extensions

import reactivemongo.bson.Macros

package object model {
  implicit val dummyModelFormat = Macros.handler[DummyModel]
}
