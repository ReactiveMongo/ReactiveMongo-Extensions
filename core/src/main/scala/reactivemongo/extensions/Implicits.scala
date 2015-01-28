package reactivemongo.extensions

import reactivemongo.api.DefaultDB

object Implicits {

  implicit class DBExtensionsBuilder(db: DefaultDB) {
    def :>(collectionName: String): DBExtensions = new DBExtensions(db(collectionName))
  }

}
