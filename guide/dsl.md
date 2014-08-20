# Query DSL

## Query Selectors

### Comparison Operators

* **$eq** Matches values that are equal to the value specified in the query.

```scala
"name" $eq "foo"
```

* **$gt** Matches values that are greater than the value specified in the query.
* **$gte** Matches values that are greater than or equal to the value specified in the query.

```scala
"age" $gt 18
"age" $gte 18
```

* **$in** Matches any of the values that exist in an array specified in the query.

```scala
 "age" $in (1, 2, 3)
```

* **$lt** Matches values that are less than the value specified in the query.
* **$lte** Matches values that are less than or equal to the value specified in the query.

```scala
"age" $lt 18
"age" $lte 18
```

* **$ne** Matches all values that are not equal to the value specified in the query.

```scala
"name" $ne "foo"
```

* **$nin** Matches values that do not exist in an array specified to the query.

```scala
"age" $nin (1, 2, 3)
```

### Logical Operators

* **$or** Joins query clauses with a logical OR returns all documents that match the conditions of either clause.

```scala
$or("qty" $lt 20 $gte 10, "sale" $eq true)
```

* **$and** Joins query clauses with a logical AND returns all documents that match the conditions of both clauses.

```scala
$and("name" $eq "foo", "surname" $eq "bar", "age" $eq 32)
```

* **$not** Inverts the effect of a query expression and returns documents that do not match the query expression.

```scala
"price" $not { _ $gte 5.1 }
```

* **$nor** Joins query clauses with a logical NOR returns all documents that fail to match both clauses.

```scala
$nor("price" $eq 1.99, "qty" $lt 20, "sale" $eq true)
```

### Element Operators

* **$exists** Matches documents that have the specified field.

```scala
"qty" $exists true
"qty" $exists false
```

* **$type** Selects documents if a field is of the specified type.

```scala
"qty".$type[BSONDouble]
"qty".$type[BSONNull.type]
```

### Evaluation Operators

* **$mod** Performs a modulo operation on the value of a field and selects documents with a specified result.

```scala
"qty" $mod (5, 0)
```

* **$regex** Selects documents where values match a specified regular expression.

```scala
"name" $regex ("^Al.*", "i")
```

* **$text** Performs text search.

```scala
$text("bake coffee cake")
$text("bake coffee cake", "turkish")
```

* **$where** Matches documents that satisfy a JavaScript expression.

```scala
$where("function () { this.credits == this.debits }")
```

### Array Operators

* **$all** Matches arrays that contain all elements specified in the query.

```scala
"size" $all ("S", "M", "L")
```

* **$elemMatch** Selects documents if element in the array field matches all the specified **$elemMatch** condition.

```scala
"array" $elemMatch ("value1" $eq 1, "value2" $gt 1)
```

* **$size** Selects documents if the array field is a specified size.

```scala
"comments" $size 12
```

## Update Operators

### Field Update Operators

* **$inc** Increments the value of the field by the specified amount.

```scala
$inc("sold" -> 1, "stock" -> -1)
```

* **$mul** Multiplies the value of the field by the specified amount.

```scala
$mul("price" -> 1.25)
```

* **$rename** Renames a field.

```scala
$rename("color" -> "colour", "realize" -> "realise")
```

* **$setOnInsert** Sets the value of a field upon document creation during an upsert. Has no effect on update operations that modify existing documents.

```scala
$setOnInsert("defaultQty" -> 500, "inStock" -> true) ++ $set("item" -> "apple")
```

* **$set** Sets the value of a field in a document.

```scala
$set("name" -> "foo", "surname" -> "bar", "age" -> 32)
```

* **$unset** Removes the specified field from a document.

```scala
$unset("name", "surname", "age")
```

* **$min** Only updates the field if the specified value is less than the existing field value.

```scala
$min("lowScore" -> 150)
```

* **$max** Only updates the field if the specified value is greater than the existing field value.

```scala
$max("highScore" -> 950)
```

* **$currentDate** Sets the value of a field to current date, either as a Date or a Timestamp.

```scala
$currentDate("lastModified" -> true, "lastModifiedTS" -> "timestamp")
```

### Array Update Operators

* **$addToSet** Adds elements to an array only if they do not already exist in the set.

```scala
$addToSet("sizes" -> "L", "colours" -> "Blue")
```

* **$pop** Removes the first or last item of an array.

```scala
$pop("scores" -> -1)
$pop("scores" -> 1)
```

* **$pull** Removes all array elements that match a specified query.

```scala
$pull("flags", "msr")
$pull("votes" $gte 6)
```

* **$push** Adds an item to an array.

```scala
$push("scores", 89)
```
