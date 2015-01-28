package reactivemongo.extensions

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, OptionValues}
import reactivemongo.bson.BSONDocument
import reactivemongo.extensions.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class DBExtensionsSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter {

  override implicit val patienceConfig = PatienceConfig(timeout = 20.seconds, interval = 1.seconds)

  val collection = MongoContext.db :> "collection"

  before {
    collection.dropSync()
  }

  after {
    collection.dropSync()
  }

  "DBExtensions" should "count" in {
    val result = for {
      _ <- collection.bulkInsert(Stream(BSONDocument("key" -> 1), BSONDocument("key" -> 2)), ordered = false)
      all <- collection.count()
      one <- collection.count(BSONDocument("key" -> 1))
      none <- collection.count(BSONDocument("key" -> 3))
    } yield (all, one, none)

    whenReady(result) { case (all, one, none) =>
      all shouldBe 2
      one shouldBe 1
      none shouldBe 0
    }
  }

  it should "find" in {
    val documents = 1 to 100 map { index => BSONDocument("key" -> index)}

    val result = for {
      _ <- collection.bulkInsert(documents.toStream, ordered = false)
      some <- collection.find(selector = BSONDocument("key" -> BSONDocument("$gt" -> 50)), page = 4, pageSize = 16)
    } yield some

    whenReady(result) { some =>
      some should have size 2
    }
  }

  it should "findOne" in {
    val result = for {
      _ <- collection.insert(BSONDocument("key" -> 1))
      one <- collection.findOne(BSONDocument("key" -> 1))
      none <- collection.findOne(BSONDocument("key" -> 2))
    } yield (one, none)

    whenReady(result) { case (one, none) =>
      one.flatMap(_.getAs[Int]("key")).value shouldBe 1
      none should be('empty)
    }
  }

  it should "findRandom" in {
    val result = for {
      _ <- collection.insert(BSONDocument("key" -> 1))
      rand <- collection.findRandom()
    } yield rand

    whenReady(result) { rand =>
      rand.flatMap(_.getAs[Int]("key")).value shouldBe 1
    }
  }

  it should "fold" in {
    val documents = 1 to 100 map { index => BSONDocument("key" -> index)}

    val result = for {
      _ <- collection.bulkInsert(documents.toStream, ordered = false)
      total <- collection.fold(0) { (total, doc: BSONDocument) =>
        total + doc.getAs[Int]("key").getOrElse(0)
      }
    } yield total

    whenReady(result) { total =>
      total shouldBe 5050
    }
  }

  it should "update" in {
    val documents = 1 to 100 map { index => BSONDocument("key" -> index)}

    val result = for {
      _ <- collection.bulkInsert(documents.toStream, ordered = false)
      before <- collection.count(BSONDocument("key" -> 100))
      _ <- collection.update(selector = BSONDocument("key" -> BSONDocument("$gt" -> 50)),
        update = BSONDocument("$set" -> BSONDocument("key" -> 100)),
        upsert = false,
        multi = true)
      after <- collection.count(BSONDocument("key" -> 100))
    } yield (before, after)

    whenReady(result) { case (before, after) =>
      before shouldBe 1
      after shouldBe 50
    }
  }
}
