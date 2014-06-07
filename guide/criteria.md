Criteria
======================

Adds a Criteria DSL for creating [ReactiveMongo](https://github.com/ReactiveMongo/ReactiveMongo) queries


### Original Query Syntax

The `reactivemongo.api.collections.GenericCollection` type provides the `find` method used to find documents matching a criteria.  It is this interaction which the DSL targets.  Originally, providing a selector to `find` had an interaction similar to:

```scala
  val cursor = collection.find(BSONDocument("firstName" -> "Jack")).cursor[BSONDocument]
```

This is, of course, still supported as the DSL does not preclude this usage.

### Criteria DSL

What the DSL *does* provide is the ablity to formulate queries thusly:

```scala
  // Using an Untyped.criteria
  {
  import Untyped._

  // The MongoDB properties referenced are not enforced by the compiler
  // to belong to any particular type.  This is what is meant by "Untyped".
  val adhoc = criteria.firstName === "Jack" && criteria.age >= 18;
  val cursor = collection.find(adhoc).cursor[BSONDocument];
  }
```

### Roadmap

This section details the functionality either currently or planned to be supported by ReactiveMongo-Criteria.

- Ability to formulate queries without requiring knowledge of document structure. *COMPLETE*
- Ability to ''type check'' query constraints by specifying a Scala type. *TBD*
- Define and add support for an [EDSL](http://scalamacros.org/usecases/advanced-domain-specific-languages.html) specific to [projections](https://github.com/ReactiveMongo/ReactiveMongo/blob/master/driver/src/test/scala/CommonUseCases.scala). *TBD*

