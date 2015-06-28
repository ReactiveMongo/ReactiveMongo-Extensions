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

package reactivemongo.extensions.fixtures

import org.scalatest._
import org.scalatest.concurrent._
import org.scalatest.time.{ Span, Seconds }
import reactivemongo.extensions.util.Logger
import reactivemongo.extensions.dao.{
  MongoContext,
  PersonBsonDao,
  EventBsonDao,
  Handlers
}, Handlers._ // extension BSON handler
import reactivemongo.extensions.Implicits.FutureOption // ~
import scala.concurrent.ExecutionContext.Implicits.global

class BsonFixturesSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfter {
  override implicit def patienceConfig = PatienceConfig(timeout = Span(20, Seconds), interval = Span(1, Seconds))

  val db = MongoContext.randomDb
  val fixtures = BsonFixtures(db)
  val personDao = new PersonBsonDao(db)
  val eventDao = new EventBsonDao(db)

  after {
    db.drop()
  }

  "A BsonFixtures" should "load persons" in {
    val futureCount = for {
      remove <- fixtures.removeAll("bson/persons.conf")
      beforeCount <- personDao.count()
      remove2 <- fixtures.removeAll("bson/persons.conf")
      insert <- fixtures.load("bson/persons.conf")
      afterCount <- personDao.count()
      person1 <- ~personDao.findByName("Ali")
      person2 <- ~personDao.findByName("Haydar")
    } yield (beforeCount, afterCount, person1, person2)

    whenReady(futureCount) {
      case (beforeCount, afterCount, person1, person2) =>
        beforeCount shouldBe 0
        afterCount shouldBe 2
        person1.fullname shouldBe "Ali Veli"
        person1.salary shouldBe 999.85
        person2.fullname shouldBe "Haydar Cabbar"
        person2.salary shouldBe 1000.0
    }
  }

  it should "load persons and events" in {
    val futureCount = for {
      remove <- fixtures.removeAll("bson/persons.conf", "bson/events.conf")
      beforeCount <- eventDao.count()
      insert <- fixtures.load("bson/persons.conf", "bson/events.conf")
      afterCount <- eventDao.count()
      event2 <- ~eventDao.findByTitle("Some movie")
    } yield (beforeCount, afterCount, event2)

    whenReady(futureCount) {
      case (beforeCount, afterCount, event2) =>
        beforeCount shouldBe 0
        afterCount shouldBe 2
        event2.organizer shouldBe "Haydar Cabbar"
        event2.location.city shouldBe "Ankara"
    }
  }
}
