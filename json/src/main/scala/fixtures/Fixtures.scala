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

import com.typesafe.config.{ Config, ConfigFactory, ConfigRenderOptions, ConfigResolveOptions }
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
  protected val resolveOptions = ConfigResolveOptions.defaults.setAllowUnresolved(true)
  protected val reserved = Set("_predef")

  protected def toString(config: Config): String = {
    config.root.render(renderOptions)
  }

  protected def toJson(config: Config): JsObject = {
    Json.parse(toString(config)).as[JsObject]
  }

  protected def resolveConfig(config: Config): Config = {
    val resolvedConfig = (config.root.keySet diff reserved).foldLeft(config) { (config, collectionName) =>
      val collectionConfig = config.getConfig(collectionName)

      val resolvedCollectionConfig = collectionConfig.root.keySet.foldLeft(collectionConfig) { (_collectionConfig, documentName) =>
        val documentConfig = _collectionConfig.getConfig(documentName).resolve(resolveOptions)
        _collectionConfig.withValue(documentName, documentConfig.root)
      }.resolve(resolveOptions)

      config.withValue(collectionName, resolvedCollectionConfig.root)
    }.resolve

    Logger.debug("Resolved Config =>\n" + toString(resolvedConfig))
    resolvedConfig
  }

  protected def processCollection(collection: JSONCollection, collectionConfig: Config): Future[Int] = {
    val documents = collectionConfig.root.keySet map { documentName =>
      val documentConfig = collectionConfig.getConfig(documentName)
      val document = toJson(documentConfig)
      Logger.debug(s"Processing ${documentName}: ${document}")
      document
    }

    val enumerator = Enumerator.enumerate(documents)
    collection.bulkInsert(enumerator)
  }

  protected def foreachCollection[T](resource: String, resources: String*)(f: (Config, String) => Future[T]): Future[Seq[T]] = {
    val config = resources.foldLeft(ConfigFactory.parseResources(resource)) { (config, resource) =>
      config.withFallback(ConfigFactory.parseResources(resource))
    }

    val resolvedConfig = resolveConfig(config)

    Future.sequence { (resolvedConfig.root.keySet diff reserved).toSeq map (f(resolvedConfig, _)) }
  }

  def load(db: () => DB, resource: String, resources: String*): Future[Seq[Int]] = {
    foreachCollection(resource, resources: _*) { (config, collectionName) =>
      Logger.debug(s"Processing ${collectionName}.")
      processCollection(db().collection[JSONCollection](collectionName), config.getConfig(collectionName))
    }
  }

  def removeAll(db: () => DB, resource: String, resources: String*): Future[Seq[LastError]] = {
    foreachCollection(resource, resources: _*) { (config, collectionName) =>
      Logger.debug(s"Removing all documents from ${collectionName}.")
      db().collection[JSONCollection](collectionName).remove(query = Json.obj(), firstMatchOnly = false)
    }
  }

  def dropAll(db: () => DB, resource: String, resources: String*): Future[Seq[Boolean]] = {
    foreachCollection(resource, resources: _*) { (config, collectionName) =>
      Logger.debug(s"Removing all documents from ${collectionName}.")
      db().collection[JSONCollection](collectionName).drop()
    }
  }

}

object Fixtures extends Fixtures
