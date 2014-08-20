// Copyright (C) 2014 Fehmi Can Saglam (@fehmicans) and contributors.
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
import reactivemongo.core.commands.LastError
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{ Json, JsObject }

trait Fixtures[T] {

  protected lazy val renderOptions = ConfigRenderOptions.concise.setJson(true).setFormatted(true)
  protected lazy val resolveOptions = ConfigResolveOptions.defaults.setAllowUnresolved(true)
  protected lazy val reserved = Set("_predef")

  def map(document: JsObject): T
  def bulkInsert(collectionName: String, enumerator: Enumerator[T]): Future[Int]
  def removeAll(collectionName: String): Future[LastError]
  def drop(collectionName: String): Future[Boolean]

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

  protected def processCollection(collectionName: String, collectionConfig: Config): Future[Int] = {
    val documents = collectionConfig.root.keySet map { documentName =>
      val documentConfig = collectionConfig.getConfig(documentName)
      val document = toJson(documentConfig)
      Logger.debug(s"Processing ${documentName}: ${document}")
      map(document)
    }

    val enumerator = Enumerator.enumerate(documents)
    bulkInsert(collectionName, enumerator)
  }

  protected def foreachCollection[A](resource: String, resources: String*)(f: (Config, String) => Future[A]): Future[Seq[A]] = {
    val config = resources.foldLeft(ConfigFactory.parseResources(this.getClass.getClassLoader, resource)) { (config, resource) =>
      config.withFallback(ConfigFactory.parseResources(this.getClass.getClassLoader, resource))
    }

    val resolvedConfig = resolveConfig(config)

    Future.sequence { (resolvedConfig.root.keySet diff reserved).toSeq map (f(resolvedConfig, _)) }
  }

  def load(resource: String, resources: String*): Future[Seq[Int]] = {
    foreachCollection(resource, resources: _*) { (config, collectionName) =>
      Logger.debug(s"Processing ${collectionName}.")
      processCollection(collectionName, config.getConfig(collectionName))
    }
  }

  def removeAll(resource: String, resources: String*): Future[Seq[LastError]] = {
    foreachCollection(resource, resources: _*) { (_, collectionName) =>
      Logger.debug(s"Removing all documents from ${collectionName}.")
      removeAll(collectionName)
    }
  }

  def dropAll(resource: String, resources: String*): Future[Seq[Boolean]] = {
    foreachCollection(resource, resources: _*) { (_, collectionName) =>
      Logger.debug(s"Removing all documents from ${collectionName}.")
      drop(collectionName)
    }
  }

}

