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
import reactivemongo.extensions.dao.Handlers._
import play.api.libs.json.Json

case class Event(
  _id: String,
  title: String,
  organizer: String,
  location: Location)

case class Location(city: String, place: String)

object Event {
  import reactivemongo.extensions.dao.Handlers._ // extensions BSON handler

  implicit val locationFormat = Macros.handler[Location]
  implicit val eventFormat = Macros.handler[Event]
}
