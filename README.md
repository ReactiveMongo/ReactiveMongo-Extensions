# ReactiveMongo Extensions

The goal of *ReactiveMongo Extensions* is to provide all the necessary tools for ReactiveMongo other than the core functionality.

Here is a complete example using Play 2.3, ReactiveMongo Extensions and Flight.js https://github.com/mertkavi/play-reactivemongo-extensions-sample/tree/0.10.5.akka23

[![Build Status](https://travis-ci.org/ReactiveMongo/ReactiveMongo-Extensions.svg?branch=0.10.5.x.akka23)](https://travis-ci.org/ReactiveMongo/ReactiveMongo-Extensions)
![Progress](http://progressed.io/bar/70?title=stable)

## Introduction

**ReactiveMongo Extensions** comes as 2 separate packages which are `reactivemongo-extensions-bson` and `reactivemongo-extensions-json`.
*reactivemongo-extensions-bson* package targets ReactiveMongo, while *reactivemongo-extensions-json* targets Play-ReactiveMongo.

#### DAOs to Serve You

DAOs provide an abstraction layer on top of ReactiveMongo adding higher level APIs like findOne, findById, count, foreach, fold, etc.
*ReactiveMongo Extensions* currently provides two DAO types for collections of documents: `reactivemongo.extensions.dao.BsonDao` for `BSONCollection` and `reactivemongo.extensions.dao.JsonDao` for `JSONCollection`. There are also two other DAO types operating on GridFS: `reactivemongo.extensions.dao.BsonFileDao` and `reactivemongo.extensions.dao.JsonFileDao`.

You will need to define a DAO for each of your models(case classes).

Below is a sample model.

```scala
import reactivemongo.bson._
import reactivemongo.extensions.dao.Handlers._

case class Person(
  _id: BSONObjectID = BSONObjectID.generate,
  name: String,
  surname: String,
  age: Int)

object Person {
  implicit val personHandler = Macros.handler[Person]
}
```

To define a BsonDao for the Person model you just need to extend BsonDao.

```scala
import reactivemongo.api.{ MongoDriver, DB }
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.DefaultBSONHandlers._
import reactivemongo.extensions.dao.BsonDao
import scala.concurrent.ExecutionContext.Implicits.global

object MongoContext {
  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))
  def db: DB = connection("reactivemongo-extensions")
}

object PersonDao extends BsonDao[Person, BSONObjectID](MongoContext.db, "persons")
```

From now on you can insert a Person instance, find it by id, find a random person or etc.

```scala
val person1 = Person(name = "foo", surname = "bar", age = 16)
val person2 = Person(name = "fehmi can", surname = "saglam", age = 32)
val person3 = Person(name = "ali", surname = "veli", age = 64)

PersonDao.insert(person1)
PersonDao.insert(Seq(person2, person3))

PersonDao.findById(person1._id)
PersonDao.findRandom(BSONDocument("age" -> BSONDocument("$ne" -> 16)))
```

Read more about BsonDao [here](guide/bsondao.md) and JsonDao [here](guide/jsondao.md).

#### Query DSL for Easy Query Construction

There are also DSL helpers for each DAO type, which are `reactivemongo.extensions.dsl.BsonDsl` and `reactivemongo.extensions.json.dsl.JsonDsl`.
DSL helpers provide utilities to easily construct JSON or BSON queries.

By mixing in or importing BsonDsl you could write the query above like this:

```scala
import reactivemongo.extensions.dsl.BsonDsl._

PersonDao.findRandom("age" $gt 16 $lt 32)
```

Read more about Query DSL [here](guide/dsl.md).

#### Criteria DSL

The Criteria DSL provides the ablity to formulate queries thusly:

```scala
  // Using an Untyped.criteria
  {
  import Untyped._

  // The MongoDB properties referenced are not enforced by the compiler
  // to belong to any particular type.  This is what is meant by "Untyped".
  val adhoc = criteria.firstName === "Jack" && criteria.age >= 18;
  val cursor = collection.find(adhoc).cursor[BSONDocument];
  }

  {
  // Using a Typed criteria which restricts properties to the
  // given type.
  import Typed._

  case class ExampleDocument (aProperty : String, another : Int)

  val byKnownProperties = criteria[ExampleDocument].aProperty =~ "^[A-Z]\\w+" &&
    criteria[ExampleDocument].another > 0;
  val cursor = collection.find(byKnownProperties).cursor[BSONDocument];
  }
```

Read more about Criteria DSL [here](guide/criteria.md).

#### Model Life Cyle

By defining a life cycle object, one can preprocess all models before being persisted or perform specific actions after life cycle events. This can be useful for updating temporal fields on all model instances before persisting.

```scala
import reactivemongo.bson._
import reactivemongo.extensions.dao.LifeCycle
import reactivemongo.extensions.dao.Handlers._
import reactivemongo.extensions.util.Logger
import org.joda.time.DateTime

case class TemporalModel(
  _id: BSONObjectID = BSONObjectID.generate,
  name: String,
  surname: String,
  createdAt: DateTime = DateTime.now,
  updatedAt: DateTime = DateTime.now)

object TemporalModel {
  implicit val temporalModelFormat = Macros.handler[TemporalModel]

  implicit object TemporalModelLifeCycle extends LifeCycle[TemporalModel, BSONObjectID] {
    def prePersist(model: TemporalModel): TemporalModel = {
      Logger.debug(s"prePersist $model")
      model.copy(updatedAt = DateTime.now)
    }
    def postPersist(model: TemporalModel): Unit = {
      Logger.debug(s"postPersist $model")
    }
    def preRemove(id: BSONObjectID): Unit = {
      Logger.debug(s"preRemove $id")
    }
    def postRemove(id: BSONObjectID): Unit = {
      Logger.debug(s"postRemove $id")
    }
    def ensuredIndexes(): Unit = {
      Logger.debug("ensuredIndexes")
    }
  }
}
```

#### Auto Indexes

ReactiveMongo Extensions support auto indexes which ensures indexes on DAO load.

```scala
object PersonDao extends {
  override val autoIndexes = Seq(
    Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
    Index(Seq("age" -> IndexType.Ascending), background = true)
  )
} with BsonDao[Person, BSONObjectID](MongoContext.db, "persons")

```

#### Default Write Concern

You can override writeConcern in your DAO definition which defaults to GetLastError().

```scala
object PersonDao extends BsonDao[Person, BSONObjectID](MongoContext.db, "persons") {
  override def defaultWriteConcern = GetLastError(j = true)
}
```

#### Fixtures for easy data loading

You can define your fixtures using HOCON. Lexical scopes are supported in addition to HOCON spec.

**persons.conf**

    _predef {
        country: TC
    }

    # "persons" collection
    persons {
        person1 {
            _id: _id_person1
            name: Ali
            surname: Veli
            fullname: ${name} ${surname}
            age: 32
            salary: 999.85
            time: 12345678900
            country: ${_predef.country}
        }

        person2 {
            _id: _id_person2
            name: Haydar
            surname: Cabbar
            fullname: ${name} ${surname}
            age: ${person1.age}
            salary: { "$double": 1000.0 }
            time: 12345678999
            country: ${_predef.country}
        }
    }


**events.conf**

    # Predefined reusable values
    _predef {
        location: {
            city: Ankara
            place: Salon
        }
    }

    # "events" collection
    events {
        event1 {
            _id: _id_event1
            title: Developer workshop
            organizer: ${persons.person1.fullname}
            location: ${_predef.location}
        }
    }

After defining your fixtures you can load them using `BsonFixtures` or `JsonFixtures`.


```scala
import reactivemongo.extensions.bson.fixtures.BsonFixtures

BsonFixtures(db).load("persons.conf", "events.conf")
```

#### ```~``` Operator for the Happy Path

While composing futures with for comprehensions, handling option values can be cumbersome.
```~``` operator converts a ```Future[Option[T]]``` to ```Future[T]```. It throws a *java.util.NoSuchElementException* if the Option is None.
Then you can check the exception in ```Future.recover```.

```scala
import reactivemongo.extensions.Implicits._

(for {
  model1 <- ~dao.findOne("none" $eq "unknown")
  model2 <- ~dao.findOne("none" $eq "unknown")
  result <- compute(model1, model2)
} yield result) recover {
  case ex: java.util.NoSuchElementException =>
    println("Option is None")
    throw ex
}

```

## Using ReactiveMongo Extensions in your project

The general format is that release a.b.c.d is compatible with ReactiveMongo a.b.c.
Current version matrix is below:


| reactivemongo-extensions-bson    | Target ReactiveMongo version |
|----------------------------------|------------------------------|
| 0.10.5.0.0.akka22                | 0.10.5.0.akka22              |

| reactivemongo-extensions-json    | Target Play-ReactiveMongo version |
|----------------------------------|-----------------------------------|
| 0.10.5.0.0.akka22                | 0.10.5.0.akka22                   |

Note: Available for Scala 2.10 and Scala 2.11.

If you use SBT, you just have to edit build.sbt and add the following:

```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo-extensions-bson" % "0.10.5.0.0.akka22"
)
```

or

```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo-extensions-json" % "0.10.5.0.0.akka22"
)
```

## Contributions
Contributions are always welcome. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Improving the performance
* Adding to the documentation
