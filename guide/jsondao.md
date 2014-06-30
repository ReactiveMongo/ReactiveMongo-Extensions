## JsonDao

### Usage

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

### API

* **bulkInsert** Bulk inserts multiple models. `prePersist` life cycle event is called for each element *before* this function and `postPersist` is called for each element after this function.

```scala
def bulkInsert(
  documents: TraversableOnce[Model],
  bulkSize: Int = bulk.MaxDocs,
  bulkByteSize: Int = bulk.MaxBulkSize): Future[Int]
```

* **count** Returns the number of documents in this collection matching the given selector.

```scala
def count(selector: JsObject = Json.obj()): Future[Int]
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
  selector: JsObject = Json.obj(),
  sort: JsObject = Json.obj("_id" -> 1),
  page: Int,
  pageSize: Int): Future[List[Model]]
```

* **findAll** Retrieves all models matching the given selector.

```scala
def findAll(
  selector: JsObject = Json.obj(),
  sort: JsObject = Json.obj("_id" -> 1)): Future[List[Model]]
```

* **findAndUpdate** Updates and returns a single model. It returns the old document by default.

```scala
def findAndUpdate(
  query: JsObject,
  update: JsObject,
  sort: JsObject = Json.obj(),
  fetchNewObject: Boolean = false,
  upsert: Boolean = false): Future[Option[Model]]
```

* **findAndRemove** Removes and returns a single model.

```scala
def findAndRemove(
  query: JsObject,
  sort: JsObject = Json.obj()): Future[Option[Model]]
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
def findOne(selector: JsObject = Json.obj()): Future[Option[Model]]
```

* **findRandom** Retrieves a random model matching the given selector.

```scala
def findRandom(selector: JsObject = Json.obj()): Future[Option[Model]]
```

* **fold** Folds the documents matching the given selector by applying the function `f`.

```scala
def fold[A](
  selector: JsObject = Json.obj(),
  sort: JsObject = Json.obj("_id" -> 1),
  state: A)(f: (A, Model) => A): Future[A]
```

* **foreach** Iterates over the documents matching the given selector and applies the function `f`.

```scala
def foreach(
  selector: JsObject = Json.obj(),
  sort: JsObject = Json.obj("_id" -> 1))(f: (Model) => Unit): Future[Unit]
```

* **insert** Inserts the given model. `prePersist` life cycle event is called *before* this function and `postPersist` is called after this function.

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
  query: JsObject,
  writeConcern: GetLastError = defaultWriteConcern,
  firstMatchOnly: Boolean = false): Future[LastError]
```

* **removeAll** Removes all documents in this collection.

```scala
def removeAll(writeConcern: GetLastError = defaultWriteConcern): Future[LastError]
```

* **removeById** Removes the document with the given ID. `preRemove` life cycle event is called *before* this function and `postRemove` is called after this function.


```scala
def removeById(id: ID, writeConcern: GetLastError = defaultWriteConcern): Future[LastError]
```

* **save** Inserts the document, or updates it if it already exists in the collection. `prePersist` life cycle event is called *before* this function and `postPersist` is called after this function.


```scala
def save(model: Model, writeConcern: GetLastError = GetLastError()): Future[LastError]
```

* **update** Updates the documents matching the given selector.

```scala
def update[U: Writes](
  selector: JsObject,
  update: U,
  writeConcern: GetLastError = defaultWriteConcern,
  upsert: Boolean = false,
  multi: Boolean = false): Future[LastError]
```

* **updateById** Updates the document with the given `id`.

```scala
def updateById[U: Writes](
  id: ID,
  update: U,
  writeConcern: GetLastError = defaultWriteConcern): Future[LastError]
```
