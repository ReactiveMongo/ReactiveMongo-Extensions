// Copyright (C) 2014 the original author or authors.
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
import org.scalatest.time.SpanSugar._
import reactivemongo.extensions.util.Logger
import reactivemongo.extensions.dao.{ MongoContext, PersonJsonDao, EventJsonDao }
import reactivemongo.extensions.Implicits._
import scala.concurrent.ExecutionContext.Implicits.global

class FixturesSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfter {

  override implicit def patienceConfig = PatienceConfig(timeout = 20 seconds, interval = 1 seconds)

  val personDao = new PersonJsonDao
  val eventDao = new EventJsonDao

  after {
    // personDao.dropSync()
    // eventDao.dropSync()
  }

  "A Fixtures" should "load persons" in {
    val futureCount = for {
      remove <- Fixtures.removeAll(MongoContext.db, "persons.conf")
      beforeCount <- personDao.count()
      insert <- Fixtures.load(MongoContext.db, "persons.conf")
      afterCount <- personDao.count()
      person1 <- ~personDao.findByName("Ali")
    } yield (beforeCount, afterCount, person1)

    whenReady(futureCount) {
      case (beforeCount, afterCount, person1) =>
        beforeCount shouldBe 0
        afterCount shouldBe 2
        person1.fullname shouldBe "Ali Veli"
    }
  }

  it should "load persons and events" in {
    val futureCount = for {
      remove <- Fixtures.removeAll(MongoContext.db, "persons.conf", "events.conf")
      beforeCount <- eventDao.count()
      insert <- Fixtures.load(MongoContext.db, "persons.conf", "events.conf")
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
