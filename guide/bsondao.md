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

```db``` and ```collectionName``` are the only required members of JsonDao. 

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
