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

package reactivemongo.extensions.dao

import java.io.OutputStream

import scala.concurrent.{ Future, ExecutionContext }

import play.api.libs.iteratee.Enumerator

import reactivemongo.api.{ BSONSerializationPack, DBMetaCommands, Cursor, DB }
import reactivemongo.api.gridfs.{
	DefaultFileToSave,
	ReadFile,
	FileToSave,
	GridFS,
	IdProducer,
	Implicits
}, Implicits.DefaultReadFileReader
import reactivemongo.bson.{ BSONDocumentReader, BSONDocumentWriter, BSONValue }
import reactivemongo.api.commands.WriteResult

import reactivemongo.extensions.dao.FileDao.ReadFileWrapper

/** Base class for all File DAO implementations.
 *
 *  @param db A [[reactivemongo.api.DB]] instance.
 *  @param collectionName Name of the collection this DAO is going to operate on.
 */
abstract class FileDao[Id <% BSONValue: IdProducer, Structure](db: => DB with DBMetaCommands, collectionName: String) {

	import FileDao.BSONReadFile

	/** Reference to the GridFS instance this FileDao operates on. */
	lazy val gfs = GridFS[BSONSerializationPack.type](db, collectionName)

	/** Finds the files matching the given selector.
	 *
	 *  @param selector Selector document
	 *  @return A cursor for the files matching the given selector.
	 */
	def find(selector: Structure)(implicit sWriter: BSONDocumentWriter[Structure], ec: ExecutionContext): Cursor[BSONReadFile] = gfs.find[Structure, BSONReadFile](selector)

	/** Retrieves the file with the given `id`. */
	def findById(id: Id)(implicit ec: ExecutionContext): ReadFileWrapper

	/* Retrieves at most one file matching the given selector. */
	def findOne(selector: Structure)(implicit sWriter: BSONDocumentWriter[Structure], ec: ExecutionContext): ReadFileWrapper =
		ReadFileWrapper(gfs, gfs.find(selector).headOption)

	/** Removes the file with the given `id`. */
	def removeById(id: Id)(implicit ec: ExecutionContext): Future[WriteResult] =
		gfs.remove(implicitly[BSONValue](id))

	/** Saves the content provided by the given enumerator with the given metadata. */
	def save(
		enumerator: Enumerator[Array[Byte]],
		file: FileToSave[BSONSerializationPack.type, BSONValue],
		chunkSize: Int = 262144
	)(implicit readFileReader: BSONDocumentReader[BSONReadFile], ec: ExecutionContext): Future[BSONReadFile] =
		gfs.save(enumerator, file, chunkSize)

	/** Saves the content provided by the given enumerator with the given metadata. */
	def save(
		enumerator: Enumerator[Array[Byte]],
		filename: Option[String],
		contentType: String
	)(implicit readFileReader: BSONDocumentReader[BSONReadFile], ec: ExecutionContext): Future[BSONReadFile] =
		gfs.save(enumerator, DefaultFileToSave(
			filename = filename, contentType = Some(contentType)
		))

}

object FileDao {
	type BSONReadFile = ReadFile[BSONSerializationPack.type, BSONValue]

	case class ReadFileWrapper(gfs: GridFS[BSONSerializationPack.type], readFile: Future[Option[BSONReadFile]]) {

		def enumerate(implicit ec: ExecutionContext): Future[Option[Enumerator[Array[Byte]]]] = readFile.map(_.map(gfs.enumerate(_)))

		def read(out: OutputStream)(implicit ec: ExecutionContext): Future[Option[Unit]] = readFile.flatMap {
			case Some(rf) => gfs.readToOutputStream(rf, out).map(Some(_))
			case None => Future.successful(None)
		}
	}

	implicit def readFileWrapperToReadFile(readFileWrapper: ReadFileWrapper): Future[Option[BSONReadFile]] = readFileWrapper.readFile

}
