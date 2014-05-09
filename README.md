# ReactiveMongo Extensions

This is a library providing DAO and DSL support for ReactiveMongo. The goal of *ReactiveMongo Extensions* is to provide all the necessary tools for ReactiveMongo other than the core functionality.

[![Build Status](https://travis-ci.org/fehmicansaglam/reactivemongo-extensions.svg?branch=0.10.x)](https://travis-ci.org/fehmicansaglam/reactivemongo-extensions)

## Introduction

*ReactiveMongo Extensions* currently provides two DAO types, which are [BsonDao](src/main/scala/dao/BsonDao.scala) for BSONCollection and [JsonDao](src/main/scala/dao/JsonDao.scala) for JSONCollection. DAOs provide an abstraction layer on top of ReactiveMongo adding higher levels of APIs like findOne, findById, count, foreach, fold, etc.

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

#### DAOs to Serve You

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
  def db: () => DB = () => connection("reactivemongo-extensions")
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

#### Easy Query Construction

There are also DSL helpers for each DAO type, which are [BsonDsl](src/main/scala/dsl/BsonDsl.scala) and [JsonDsl](src/main/scala/dsl/JsonDsl.scala). DSL helpers provide utilities to easily construct JSON or BSON queries.

By mixing or importing BsonDsl you could write the query above like this:

```scala
import reactivemongo.extensions.dsl.BsonDsl._

PersonDao.findRandom($ne("age" -> 16))
```

#### Functional DSL

Even better there is also an infix version for each DSL type.

```scala
import reactivemongo.extensions.dsl.functional.BsonDsl._

PersonDao.findRandom("age" $gt 16 $lt 32)
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

Each type has its own dedicated documentation page, however API for all types are very similar. You need to define a DAO for each of your models. A DAO needs a ```db``` and a ```collectionName```.

[BsonDao](guide/bsondao.md)

[BsonDsl](guide/bsondsl.md)

[JsonDao](guide/jsondao.md)

[JsonDsl](guide/jsondsl.md)

## Using ReactiveMongo Extensions in your project

The general format is that release a.b.c.d is compatible with ReactiveMongo a.b.c.
Current version matrix is below:

| ReactiveMongo Extensions Release | Target ReactiveMongo version |
|----------------------------------|------------------------------|
| 0.10.0.1                         | 0.10.0                       |
| 0.10.0.2-SNAPSHOT                | 0.10.0                       |
| 0.11.0.0-SNAPSHOT                | 0.11.0-SNAPSHOT              |

Note: Only available for scala 2.10.

If you use SBT, you just have to edit build.sbt and add the following:

```scala
libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "reactivemongo-extensions" % "0.10.0.1"
)
```

Or if you want to be on the bleeding edge using snapshots:

```scala
resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "reactivemongo-extensions" % "0.10.0.2-SNAPSHOT"
)
```

## Contributions
Contributions are always welcome. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Improving the performance
* Adding to the documentation






