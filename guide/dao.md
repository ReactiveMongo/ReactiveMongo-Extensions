## BsonDao

### Usage

BsonDao operates on reactivemongo.api.collections.default.BSONCollection. You will need to define a DAO for each of your models(case classes).

Below is a sample model.

```scala
case class Person(
  _id: BSONObjectID = BSONObjectID.generate,
  name: String,
  surname: String,
  age: Int)
```

Now let's define a BsonDao for this model.

```scala
object PersonDao extends BsonDao[Person] {
  def db: DB = ???
  val collectionName: String = "persons"
}
```

```db``` and ```collectionName``` are the only required members of BsonDao.

### API


#### findOne
```scala
def findOne(selector: BSONDocument): Future[Option[T]]
```

#### findById
```scala
def findById(id: BSONValue): Future[Option[T]]
```

#### insert
```scala
def insert(document: T): Future[LastError]
```

```scala
def insert(documents: TraversableOnce[T]): Future[Int]
```

#### updateById
```scala
def updateById(id: BSONValue,
               update: BSONDocument,
               writeConcern: GetLastError = GetLastError(),
               upsert: Boolean = false,
               multi: Boolean = false): Future[LastError]
```

#### save
```scala
def save(document: T, writeConcern: GetLastError = GetLastError())
```

#### count
```scala
def count(selector: BSONDocument = BSONDocument.empty): Future[Int]
```

#### foreach
```scala
def foreach(selector: BSONDocument = BSONDocument.empty,
            sort: BSONDocument = BSONDocument(idField -> 1))(f: (T) => Unit): Future[Unit]
```

#### fold
```scala
def fold[A](selector: BSONDocument = BSONDocument.empty,
            sort: BSONDocument = BSONDocument(idField -> 1),
            state: A)(f: (A, T) => A): Future[A]
```

#### drop
```scala
def drop(): Future[Boolean]
```

#### dropSync
```scala
def dropSync(timeout: Duration = 10 seconds): Boolean
```


## JsonDao

JsonDao operates on play.modules.reactivemongo.json.collection.JSONCollection.
If you are using Play-ReactiveMongo plugin you will need to use JsonDao.

Below is a sample model.

```scala
import reactivemongo.bson.BSONObjectID

case class Person(
  _id: BSONObjectID = BSONObjectID.generate,
  name: String,
  surname: String,
  age: Int)
```

Now let's define a companion object and a JsonDao for this model.
As a best practice companion object should not contain DB related functions.
These functions should be in a DAO object.
Companion object should provide helper functions for the model like transformations, validation, etc.

```scala
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.BSONFormats._

object Person {
  implicit val personFormat = Json.format[Person]
}

object PersonDao
  extends JsonDao[Person, BSONObjectID](() => ReactiveMongoPlugin.db, "persons"){
  // some high level db functions
}
```

As seen in the example above ```db``` and ```collectionName``` are the only required parameters of JsonDao.
If you want your indexes to be ensured on DAO load, you can modify the DAO definition like below.

```scala
import reactivemongo.api.indexes.{ Index, IndexType }

object PersonDao extends {
  override val autoIndexes = Seq(
    Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
    Index(Seq("age" -> IndexType.Ascending), background = true)
  )
} with JsonDao[Person, BSONObjectID](() => ReactiveMongoPlugin.db, "persons") {
  // some high level db functions
}
```

