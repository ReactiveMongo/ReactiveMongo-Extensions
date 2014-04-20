# ReactiveMongo Extensions

This is an extension library providing DAO and DSL support for ReactiveMongo. 

[![Build Status](https://travis-ci.org/fehmicansaglam/reactivemongo-extensions.svg?branch=master)](https://travis-ci.org/fehmicansaglam/reactivemongo-extensions)

## BsonDao

### findOne 
```scala 
def findOne(selector: BSONDocument): Future[Option[T]]
```

### findById
```scala 
def findById(id: String): Future[Option[T]]
```

### insert
```scala
def insert(document: BSONDocument): Future[LastError]
```

```scala
def insert(document: T): Future[LastError]
```

```scala
def insert(documents: TraversableOnce[T]): Future[Int]
```

### updateById
```scala
def updateById(id: String,
               update: BSONDocument,
               writeConcern: GetLastError = GetLastError(),
               upsert: Boolean = false,
               multi: Boolean = false): Future[LastError]
```

### count
```scala
def count(selector: Option[BSONDocument] = None): Future[Int]
```

### foreach
```scala
def foreach(selector: BSONDocument = BSONDocument.empty,
            sort: BSONDocument = BSONDocument("_id" -> 1))(f: (T) => Unit): Future[Unit]
```

### fold
```scala
def fold[A](selector: BSONDocument = BSONDocument.empty,
            sort: BSONDocument = BSONDocument("_id" -> 1),
            state: A)(f: (A, T) => A): Future[A]
```

### drop
```scala
def drop(): Future[Boolean] 
```

### dropSync
```scala
def dropSync(timeout: Duration = 10 seconds): Boolean 
```

## BsonDsl

### $doc
```scala
// Input
$doc("name" -> "foo", "surname" -> "bar", "age" -> 32) 
// Output
BSONDocument("name" -> "foo", "surname" -> "bar", "age" -> 32)

// Input
$doc("age", $gt(50), $lt(60))
// Output
BSONDocument("age" -> BSONDocument("$gt" -> 50, "$lt" -> 60))
```

### $ne
```scala
// Input
$ne("name" -> "foo")
// Output
BSONDocument("name" -> BSONDocument("$ne" -> "foo"))
```

### $gt
```scala
// Input
$gt("age" -> 16)
// Output
BSONDocument("age" -> BSONDocument("$gt" -> 16))
```

### $gte
```scala
// Input
$gte("age" -> 16)
// Output
BSONDocument("age" -> BSONDocument("$gte" -> 16))
```

### $in
```scala
// Input
$in("age", 1, 2, 3)
// Output
BSONDocument("age" -> BSONDocument("$in" -> BSONArray(1, 2, 3)))
```

### $lt
```scala
// Input
$lt("age" -> 16)
// Output
BSONDocument("age" -> BSONDocument("$lt" -> 16))
```

### $lte
```scala
// Input
$lte("age" -> 16)
// Output
BSONDocument("age" -> BSONDocument("$lte" -> 16))
```

### $nin
```scala
// Input
$nin("age", 1, 2, 3)
// Output
BSONDocument("age" -> BSONDocument("$nin" -> BSONArray(1, 2, 3)))
```

### $set
```scala
// Input
$set("name" -> "foo", "surname" -> "bar", "age" -> 32)
// Output
BSONDocument("$set" -> BSONDocument("name" -> "foo", "surname" -> "bar", "age" -> 32))
```

### $unset
```scala
// Input
$unset("name", "surname", "age")
// Output
BSONDocument("$unset" -> BSONDocument("name" -> "", "surname" -> "", "age" -> ""))
```
