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
 *
 * Created on: Jun 22, 2014
 */
package reactivemongo.extensions.dsl.criteria

import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.reflect.runtime.universe._

/**
 * The '''Typed''' `object` provides the ability to ''lift'' an arbitrary type
 * `T` into the [[reactivemongo.extensions.dsl.criteria]] world.  Each property
 * is represented as a [[reactivemongo.extensions.dsl.criteria.Term]].
 *
 *
 * @author svickers
 *
 */
object Typed {
  /// Class Types
  class PropertyAccess[T]
      extends Dynamic {
    def selectDynamic(property: String) = macro PropertyAccess.select[T];
  }

  object PropertyAccess {
    def select[T: c.WeakTypeTag](c: Context)(property: c.Expr[String]) =
      {
        import c.universe._
        import c.mirror._

        val tree = (c.prefix.tree, property.tree) match {
          case (
            TypeApply(
              Select(_, _),
              List(parentType)
              ),
            st @ Literal(Constant(name: String))
            ) =>
            val accessor = parentType.tpe.member(newTermName(name)) orElse {
              c.abort(
                c.enclosingPosition,
                s"$name is not a member of ${parentType.tpe}"
              );
            }

            Apply(
              Select(
                New(TypeTree(c.universe.typeOf[Term[Any]])),
                nme.CONSTRUCTOR
              ),
              List(st)
            );

          case other =>
            c.abort(c.enclosingPosition, s"only property access is supported: $other");
        }

        c.Expr[Any](tree);
      }
  }

  /**
   * The criteria method produces a type which enforces the existence of
   * property names within ''T''.
   */
  def criteria[T] = new PropertyAccess[T];
}

