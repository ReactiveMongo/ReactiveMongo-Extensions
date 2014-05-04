## JsonDsl

There are 2 different DSLs which are reactivemongo.extensions.dsl.JsonDsl and reactivemongo.extensions.dsl.functional.JsonDsl.
functional.JsonDsl provides infix syntax.

### $doc
```scala
// Input
$doc("name" -> "foo", "surname" -> "bar", "age" -> 32)
// Output
Json.obj("name" -> "foo", "surname" -> "bar", "age" -> 32)

// Input
$docx("age", $gtx(50), $ltx(60))
// Infix syntax
"age" $gt 50 $lt 60
// Output
Json.obj("age" -> Json.obj("$gt" -> 50, "$lt" -> 60))
```

### $ne
```scala
// Input
$ne("name" -> "foo")
// Infix syntax
"name" $ne "foo"
// Output
Json.obj("name" -> Json.obj("$ne" -> "foo"))
```

### $gt
```scala
// Input
$gt("age" -> 16)
// Infix syntax
"age" $gt 16
// Output
Json.obj("age" -> Json.obj("$gt" -> 16))
```

### $gte
```scala
// Input
$gte("age" -> 16)
// Infix syntax
"age" $gte 16
// Output
Json.obj("age" -> Json.obj("$gte" -> 16))
```

### $in
```scala
// Input
$in("age", Seq(1, 2, 3))
// Infix syntax
"age" $in Seq(1, 2, 3)
// Output
Json.obj("age" -> Json.obj("$in" -> Json.arr(1, 2, 3)))
```

### $lt
```scala
// Input
$lt("age" -> 16)
// Infix syntax
"age" $lt 16
// Output
Json.obj("age" -> Json.obj("$lt" -> 16))
```

### $lte
```scala
// Input
$lte("age" -> 16)
// Infix syntax
"age" $lte 16
// Output
Json.obj("age" -> Json.obj("$lte" -> 16))
```

### $nin
```scala
// Input
$nin("age", Seq(1, 2, 3))
// Infix syntax
"age" $nin Seq(1, 2, 3)
// Output
Json.obj("age" -> Json.obj("$nin" -> Json.arr(1, 2, 3)))
```

### $set
```scala
// Input
$set("name" -> "foo", "surname" -> "bar", "age" -> 32)
// Output
Json.obj("$set" -> Json.obj("name" -> "foo", "surname" -> "bar", "age" -> 32))
```

### $unset
```scala
// Input
$unset("name", "surname", "age")
// Output
Json.obj("$unset" -> Json.obj("name" -> "", "surname" -> "", "age" -> ""))
```
