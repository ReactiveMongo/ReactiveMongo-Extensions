# ReactiveMongo Extensions

This is an extension library providing DAO and DSL support for ReactiveMongo.

[![Build Status](https://travis-ci.org/fehmicansaglam/reactivemongo-extensions.svg?branch=master)](https://travis-ci.org/fehmicansaglam/reactivemongo-extensions)

## Using ReactiveMongo Extensions in your project

The latest release is 0.11.0.0-SNAPSHOT which is compatible with ReactiveMongo 0.11.0-SNAPSHOT.
The general format is that release a.b.c.d is compatible with ReactiveMongo a.b.c.

Note: Only available for scala 2.10.

If you use SBT, you just have to edit build.sbt and add the following:

```scala
resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "reactivemongo-extensions" % "0.11.0.0-SNAPSHOT"
)
```

## Contributions
Contributions are always welcome. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Improving the performance
* Adding to the documentation


## Introduction

[BSON DAO](guide/bsondao.md)

[BSON DSL](guide/bsondsl.md)

[JSON DAO](guide/jsondao.md)

[JSON DSL](guide/bsondao.md)
