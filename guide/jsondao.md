# JSON DAO

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
object PersonDao extends {
  override val autoIndexes = Seq(
    Index(Seq("name" -> IndexType.Ascending), unique = true, background = true),
    Index(Seq("age" -> IndexType.Ascending), background = true)
  )
} with JsonDao[Person, BSONObjectID](() => ReactiveMongoPlugin.db, "persons") {
  // some high level db functions
}
```
