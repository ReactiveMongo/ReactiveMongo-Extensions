## BsonDao

### Usage

BsonDao operates on reactivemongo.api.collections.default.BSONCollection. You will need to define a DAO for each of your models(case classes).

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
  def db(): DB = connection("reactivemongo-extensions")
}

object PersonDao extends BsonDao[Person, BSONObjectID](MongoContext.db, "persons")
```

As seen in the example above ```db``` and ```collectionName``` are the only required parameters of BsonDao.
If you want your indexes to be ensured on DAO load, you can modify the DAO definition like below.

```scala
import reactivemongo.api.indexes.{ Index, IndexType }

object PersonDao extends {
  override val autoIndexes = Seq(
    Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
    Index(Seq("age" -> IndexType.Ascending), background = true)
  )
} with BsonDao[Person, BSONObjectID](MongoContext.db, "persons") {
  // some high level db functions
}
```

### API

* **bulkInsert** Bulk inserts multiple models.

```scala
def bulkInsert(models: TraversableOnce[Model], bulkSize: Int, bulkByteSize: Int): Future[Int]
```

* **count** Returns the number of documents in this collection matching the given selector.

```scala
def count(selector: BSONDocument = BSONDocument.empty): Future[Int]
```

* **drop** Drops this collection

```scala
def drop(): Future[Boolean]
```

* **dropSync** Drops this collection and awaits until it has been dropped or a timeout has occured.

```scala
def dropSync(timeout: Duration): Boolean
```

* **find** Retrieves models by page matching the given selector.

```scala
def find(
  selector: BSONDocument = BSONDocument.empty,
  sort: BSONDocument = BSONDocument("_id" -> 1),
  page: Int,
  pageSize: Int): Future[List[Model]]
```

* **findAll** Retrieves all models matching the given selector.

```scala
def findAll(
  selector: BSONDocument = BSONDocument.empty,
  sort: BSONDocument = BSONDocument("_id" -> 1)): Future[List[Model]]
```

* **findAndUpdate** Updates and returns a single model. It returns the old document by default.

```scala
def findAndUpdate(
  query: BSONDocument,
  update: BSONDocument,
  sort: BSONDocument = BSONDocument.empty,
  fetchNewObject: Boolean = false,
  upsert: Boolean = false): Future[Option[Model]]
```

* **findAndRemove** Removes and returns a single model.

```scala
def findAndRemove(
  query: BSONDocument,
  sort: BSONDocument = BSONDocument.empty): Future[Option[Model]]
```

* **findById** Retrieves the model with the given `id`.

```scala
def findById(id: ID): Future[Option[Model]]
```

* **findByIds** Retrieves the models with the given `ids`.

```scala
def findByIds(ids: ID*): Future[List[Model]]
```

* **findOne** Retrieves at most one model matching the given selector.

```scala
def findOne(selector: BSONDocument = BSONDocument.empty): Future[Option[Model]]
```

* **findRandom** Retrieves a random model matching the given selector.

```scala
def findRandom(selector: BSONDocument = BSONDocument.empty): Future[Option[Model]]
```

* **fold** Folds the documents matching the given selector by applying the function `f`.

```scala
def fold[A](
  selector: BSONDocument = BSONDocument.empty,
  sort: BSONDocument = BSONDocument("_id" -> 1),
  state: A)(f: (A, Model) => A): Future[A]
```

* **foreach** Iterates over the documents matching the given selector and applies the function `f`.

```scala
def foreach(
  selector: BSONDocument = BSONDocument.empty,
  sort: BSONDocument = BSONDocument("_id" -> 1))(f: (Model) => Unit): Future[Unit]
```

* **insert** Inserts the given model.

```scala
def insert(model: Model, writeConcern: GetLastError = defaultWriteConcern): Future[LastError]
```

* **listIndexes** Lists indexes that are currently ensured in this collection.

```scala
def listIndexes(): Future[List[Index]]
```

* **remove** Removes model(s) matching the given selector.

```scala
def remove(
  query: BSONDocument,
  writeConcern: GetLastError = defaultWriteConcern,
  firstMatchOnly: Boolean = false): Future[LastError]
```

* **removeAll** Removes all documents in this collection.

```scala
def removeAll(writeConcern: GetLastError = defaultWriteConcern): Future[LastError]
```

* **removeById** Removes the document with the given ID.

```scala
def removeById(id: ID, writeConcern: GetLastError = defaultWriteConcern): Future[LastError]
```

* **save** Inserts the document, or updates it if it already exists in the collection.

```scala
def save(model: Model, writeConcern: GetLastError = defaultWriteConcern): Future[LastError]
```

* **update** Updates the documents matching the given selector.

```scala
def update[U: BSONDocumentWriter](
  selector: BSONDocument,
  update: U,
  writeConcern: GetLastError = defaultWriteConcern,
  upsert: Boolean = false,
  multi: Boolean = false): Future[LastError]
```

* **updateById** Updates the document with the given `id`.

```scala
def updateById[U: BSONDocumentWriter](
  id: ID,
  update: U,
  writeConcern: GetLastError = defaultWriteConcern): Future[LastError]
```
