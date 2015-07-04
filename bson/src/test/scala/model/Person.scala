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

package reactivemongo.extensions.model

import reactivemongo.bson._

case class Person(
  _id: String,
  name: String,
  surname: String,
  fullname: String,
  age: Int,
  salary: Double,
  time: Long,
  country: String)

object Person {
  import reactivemongo.extensions.dao.Handlers._ // extensions BSON handler
  implicit val personFormat = Macros.handler[Person]
}
