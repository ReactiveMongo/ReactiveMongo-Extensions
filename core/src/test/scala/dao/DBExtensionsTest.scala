package dao

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, OptionValues}
import reactivemongo.bson.BSONDocument
import reactivemongo.extensions.Implicits._
import reactivemongo.extensions.dao.MongoContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class DBExtensionsTest
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter {

  override implicit def patienceConfig = PatienceConfig(timeout = 20.seconds, interval = 1.seconds)

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

}
