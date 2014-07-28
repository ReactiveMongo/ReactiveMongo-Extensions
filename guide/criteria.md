Criteria
======================

Adds a Criteria DSL for creating [ReactiveMongo](https://github.com/ReactiveMongo/ReactiveMongo) queries


## Overview

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

Another form which achieves the same result is to use one of the `where` methods available:

```scala
  // Using one of the Untyped.where overloads
  {
  import Untyped._

  val cursor = collection.find(
    where (_.firstName === "Jack" && _.age >= 18)
	).cursor[BSONDocument];
  }
```

There are overloads for between 1 and 22 place holders using the `where` method.  Should more than 22 be needed, then the 1 argument version should be used with a named parameter.  This allows an infinite number of property constraints to be specified.

<<<<<<< HEAD
For situations where the MongoDB document structure is well known and a developer wishes enforce property existence, the `Typed` Criteria can be used:

```scala
  {
  // Using a Typed criteria which restricts properties to the
  // given type.
  import Typed._

  case class ExampleDocument (aProperty : String, another : Int)

  val byKnownProperties = criteria[ExampleDocument].aProperty =~ "^[A-Z]\\w+" &&
    criteria[ExampleDocument].another > 0;
  val cursor = collection.find(byKnownProperties).cursor[BSONDocument];
  }
```

Note that `Typed` and `Untyped` serve different needs.  When the structure of a document collection is both known ''and'' identified as static, `Typed` makes sense to employ.  However, `Untyped` is compelling when document structure can vary within a collection.  These are considerations which can easily vary between projects and even within different modules of one project.

=======
>>>>>>> origin/0.10.x
### Roadmap

This section details the functionality either currently or planned to be supported by ReactiveMongo-Criteria.

- Ability to formulate queries without requiring knowledge of document structure. *COMPLETE*
<<<<<<< HEAD
- Ability to ''type check'' query constraints by specifying a Scala type. *IN PROGRESS*
=======
- Ability to ''type check'' query constraints by specifying a Scala type. *TBD*
>>>>>>> origin/0.10.x
- Define and add support for an [EDSL](http://scalamacros.org/usecases/advanced-domain-specific-languages.html) specific to [projections](https://github.com/ReactiveMongo/ReactiveMongo/blob/master/driver/src/test/scala/CommonUseCases.scala). *TBD*


## Operators

When using the Criteria DSL, the fact that the operators adhere to the expectations of both programmers and Scala precedences, most uses will "just work."  For example, explicitly defining grouping is done with parentheses, just as you would do with any other bit of Scala code.

For the purposes of the operator API reference, assume the following code is in scope:

```scala
import reactivemongo.extensions.dsl.criteria.Untyped._
```

### Comparison Operators

With the majority of comparison operators, keep in mind that the definition of their ordering is dependent on the type involved.  For example, strings will use lexigraphical ordering whereas numbers use natural ordering.

* **===**, **@==** Matches properties based on value equality.

```scala
criteria.aProperty === "value"
```

```scala
criteria.aProperty @== "value"
```

* **<>**, **=/=**, **!==** Matches properties which do not have the given value.

```scala
criteria.aProperty <> "value"
```

```scala
criteria.aProperty =/= "value"
```

```scala
criteria.aProperty !== "value"
```

* **<** Matches properties which compare "less than" a given value.

```scala
criteria.aNumber < 99
```

* **<=** Matches properties which compare "less than or equal to" a given value.

```scala
criteria.aNumber <= 99
```

* **>** Matches properties which compare "greater than" a given value.

```scala
criteria.aProperty > "Alice"
```

* **>=** Matches properties which compare "greater than or equal to" a given value.

```scala
criteria.aNumber >= 100
```

### Existence Operators

* **exists** Matches any document which has the specified field.

```scala
criteria.aProperty.exists
```

* **in** Matches properties which are arrays and have one of the given values.

```scala
criteria.anArray.in (1, 2, 3, 4, 5)
```

* **all** Matches array properties which contain all of the given values.

```scala
criteria.strings.all ("hello", "world")
```

### String Operators

* **=~** Matches a string property which satisfies the given regular expression `String`.

```scala
criteria.aProperty =~ """^(value)|(someting\s+else)"""
```

* **!~** Matches a string property which does _not_ satisfy the given regular expression `String`.

```scala
criteria.aProperty !~ """\d+"""
```

### Logical Operators

* **!** The unary not operator provides logical negation of an `Expression`.

```scala
!(criteria.aProperty === "value")
```

* **&&** Defines logical conjunction (''AND'').

```scala
criteria.aProperty === "value" && criteria.another > 0
```

* **!&&** Defines negation of conjunction (''NOR'').

```scala
criteria.aProperty === "value" !&& criteria.aProperty @== "other value"
```

* **||** Defines logical disjunction (''OR'').

```scala
criteria.aProperty === "value" || criteria.aProperty === "other value"
```

