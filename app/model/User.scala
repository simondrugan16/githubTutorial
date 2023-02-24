package model

import play.api.libs.json.{Json, OFormat}

case class User(login: String,
                created_at: String,
                location: Option[String],
                followers: Int,
                following: Int)

object User {
  implicit val formats: OFormat[User] = Json.format[User]
}
