# JSON DAO

JsonDao operates on play.modules.reactivemongo.json.collection.JSONCollection. You will need to define a DAO for each of your models(case classes).

Below is a sample model.

```scala
case class Person(
  _id: BSONObjectID = BSONObjectID.generate,
  name: String, 
  surname: String, 
  age: Int)
```

Now let's define a JsonDao for this model.

```scala
object PersonDao extends JsonDao[Person] {
  def db: DB = ???
  val collectionName: String = "persons"
}
```

```db``` and ```collectionName``` are the only required members of JsonDao. If you are using Play ReactiveMongo plugin, you can insert the db provided by the plugin in your Dao.

```scala
object PersonDao extends JsonDao[Person] {
  def db: DB = ReactiveMongoPlugin.db
  val collectionName: String = "persons"
}
```
