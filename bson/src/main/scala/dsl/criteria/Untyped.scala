/*
 * Copyright 2013 Steve Vickers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactivemongo.extensions.dsl.criteria

import scala.language.dynamics

/**
 * The '''Untyped''' type defines the behaviour expected of queries where the
 * MongoDB document may not correspond to a Scala type known to the system
 * using this abstraction.
 */
sealed trait Untyped
    extends Dynamic {
  def selectDynamic(field: String): Term[Any] =
    Term[Any](field);
}

object Untyped {
  /**
   * The criteria property is a ''factory'' of '''Untyped''' instances.
   */
  val criteria = new Untyped {};

  def where(block: (Untyped) => Expression): Expression =
    block(criteria);

  def where(block: (Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);

  def where(block: (Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped, Untyped) => Expression): Expression =
    block(criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria, criteria);
}

