package reactivemongo.extensions.dsl

import org.scalatest.{FlatSpec, Matchers}
import reactivemongo.bson.BSONDocument
import reactivemongo.extensions.dsl.BsonDsl._

class BsonDslSpec extends FlatSpec with Matchers {

  "A BsonDsl" should "create BSONDocument" in {
    val expected = BSONDocument(
      "name" -> "jack",
      "age" -> 18,
      "months" -> Seq(1, 2, 3),
      "details" -> BSONDocument(
        "salary" -> 455.5,
        "checked" -> true
      )
    )

    val actual = ("name" := "jack") ~ ("age" := 18) ~ ("months" := Seq(1, 2, 3)) ~ ("details" := {
      ("salary" := 455.5) ~ ("checked" := true)
    })

    actual shouldBe expected
  }

}
