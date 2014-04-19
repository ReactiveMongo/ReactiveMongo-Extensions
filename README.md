# Extensions for ReactiveMongo

[![Build Status](https://travis-ci.org/fehmicansaglam/reactivemongo-extensions.svg?branch=master)](https://travis-ci.org/fehmicansaglam/reactivemongo-extensions)

## DAO

### BSonDao\[T\]

#### findById(id: String): Future[Option[T]]

#### findOne(selector: BSONDocument): Future[Option[T]]

#### insert(document: BSONDocument): Future[LastError]

#### insert(document: T): Future[LastError]

#### updateById(id: String, selector: BSONDocument): Future[LastError]

#### count(selector: Option[BSONDocument] = None): Future[Int]

#### foreach(selector: BSONDocument = BSONDocument.empty, sort: BSONDocument = BSONDocument("_id" -> 1))(f: (T) => Unit): Future[Unit]

#### fold\[A\](selector: BSONDocument = BSONDocument.empty, sort: BSONDocument = BSONDocument("_id" -> 1), state: A)(f: (A, T) => A): Future[A]

## DSL
