// Copyright (C) 2014 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package reactivemongo.extensions.fixtures

import com.typesafe.config.{ Config, ConfigFactory, ConfigRenderOptions }
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.extensions.util.Logger
import reactivemongo.api.DB
import reactivemongo.core.commands.LastError
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{ Json, JsObject }

trait Fixtures {

  protected val renderOptions = ConfigRenderOptions.concise.setJson(true).setFormatted(true)
  protected val reserved = Set("_predef")

  protected def toJson(config: Config): JsObject = {
    Json.parse(config.root.render(renderOptions)).as[JsObject]
  }

  protected def processCollection(collection: JSONCollection, config: Config): Future[Int] = {
    val documents = config.root.keySet map { documentName =>
      val document = toJson(config.getConfig(documentName))
      Logger.debug(s"Processing ${documentName}: ${document}")
      document
    }

    val enumerator = Enumerator.enumerate(documents)
    collection.bulkInsert(enumerator)
  }

  protected def foreach[T](resource: String)(f: (Config, String) => Future[T]): Future[Seq[T]] = {
    val config = ConfigFactory.parseResources(resource).resolve
    Future.sequence { (config.root.keySet diff reserved).toSeq map (f(config, _)) }
  }

  def load(db: () => DB, resource: String): Future[Seq[Int]] = {
    foreach(resource) { (config, collectionName) =>
      Logger.debug(s"Processing ${collectionName}.")
      processCollection(db().collection[JSONCollection](collectionName), config.getConfig(collectionName))
    }
  }

  def removeAll(db: () => DB, resource: String): Future[Seq[LastError]] = {
    foreach(resource) { (config, collectionName) =>
      Logger.debug(s"Removing all documents from ${collectionName}.")
      db().collection[JSONCollection](collectionName).remove(query = Json.obj(), firstMatchOnly = false)
    }
  }

  def dropAll(db: () => DB, resource: String): Future[Seq[Boolean]] = {
    foreach(resource) { (config, collectionName) =>
      Logger.debug(s"Removing all documents from ${collectionName}.")
      db().collection[JSONCollection](collectionName).drop()
    }
  }

}

object Fixtures extends Fixtures
