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

package reactivemongo.extensions.util

import org.slf4j.{ LoggerFactory, Logger => Slf4jLogger }
import scala.util.control.NonFatal

/** Typical logger interface.
 */
trait LoggerLike {

	/** The underlying SLF4J Logger.
	 */
	def logger: Slf4jLogger

	/** `true` if the logger instance is enabled for the `TRACE` level.
	 */
	def isTraceEnabled = logger.isTraceEnabled

	/** `true` if the logger instance is enabled for the `DEBUG` level.
	 */
	def isDebugEnabled = logger.isDebugEnabled

	/** `true` if the logger instance is enabled for the `INFO` level.
	 */
	def isInfoEnabled = logger.isInfoEnabled

	/** `true` if the logger instance is enabled for the `WARN` level.
	 */
	def isWarnEnabled = logger.isWarnEnabled

	/** `true` if the logger instance is enabled for the `ERROR` level.
	 */
	def isErrorEnabled = logger.isErrorEnabled

	/** Logs a message with the `TRACE` level.
	 *
	 *  @param message the message to log
	 */
	def trace(message: => String) {
		if (logger.isTraceEnabled) logger.trace(message)
	}

	/** Logs a message with the `TRACE` level.
	 *
	 *  @param message the message to log
	 *  @param error the associated exception
	 */
	def trace(message: => String, error: => Throwable) {
		if (logger.isTraceEnabled) logger.trace(message, error)
	}

	/** Logs a message with the `DEBUG` level.
	 *
	 *  @param message the message to log
	 */
	def debug(message: => String) {
		if (logger.isDebugEnabled) logger.debug(message)
	}

	/** Logs a message with the `DEBUG` level.
	 *
	 *  @param message the message to log
	 *  @param error the associated exception
	 */
	def debug(message: => String, error: => Throwable) {
		if (logger.isDebugEnabled) logger.debug(message, error)
	}

	/** Logs a message with the `INFO` level.
	 *
	 *  @param message the message to log
	 */
	def info(message: => String) {
		if (logger.isInfoEnabled) logger.info(message)
	}

	/** Logs a message with the `INFO` level.
	 *
	 *  @param message the message to log
	 *  @param error the associated exception
	 */
	def info(message: => String, error: => Throwable) {
		if (logger.isInfoEnabled) logger.info(message, error)
	}

	/** Logs a message with the `WARN` level.
	 *
	 *  @param message the message to log
	 */
	def warn(message: => String) {
		if (logger.isWarnEnabled) logger.warn(message)
	}

	/** Logs a message with the `WARN` level.
	 *
	 *  @param message the message to log
	 *  @param error the associated exception
	 */
	def warn(message: => String, error: => Throwable) {
		if (logger.isWarnEnabled) logger.warn(message, error)
	}

	/** Logs a message with the `ERROR` level.
	 *
	 *  @param message the message to log
	 */
	def error(message: => String) {
		if (logger.isErrorEnabled) logger.error(message)
	}

	/** Logs a message with the `ERROR` level.
	 *
	 *  @param message the message to log
	 *  @param error the associated exception
	 */
	def error(message: => String, error: => Throwable) {
		if (logger.isErrorEnabled) logger.error(message, error)
	}

}

/** A Play logger.
 *
 *  @param logger the underlying SL4FJ logger
 */
class Logger(val logger: Slf4jLogger) extends LoggerLike

/** High-level API for logging operations.
 *
 *  For example, logging with the default application logger:
 *  {{{
 *  Logger.info("Hello!")
 *  }}}
 *
 *  Logging with a custom logger:
 *  {{{
 *  Logger("my.logger").info("Hello!")
 *  }}}
 *
 */
object Logger extends LoggerLike {

	/** The 'application' logger.
	 */
	lazy val logger = LoggerFactory.getLogger("reactivemongo.extensions")

	/** Obtains a logger instance.
	 *
	 *  @param name the name of the logger
	 *  @return a logger
	 */
	def apply(name: String): Logger = new Logger(LoggerFactory.getLogger(name))

	/** Obtains a logger instance.
	 *
	 *  @param clazz a class whose name will be used as logger name
	 *  @return a logger
	 */
	def apply[T](clazz: Class[T]): Logger = new Logger(LoggerFactory.getLogger(clazz))

}

