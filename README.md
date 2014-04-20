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

## DSL
