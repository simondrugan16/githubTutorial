package model

import play.api.libs.json.{Json, OFormat}

case class UserRepo(name: String,
                    owner: Owner)

object UserRepo {
  implicit val format: OFormat[UserRepo] = Json.format[UserRepo]
}

case class Owner(login: String)

object Owner {
  implicit val format: OFormat[Owner] = Json.format[Owner]
}

