package model

import play.api.libs.json.{Json, OFormat}

case class GithubCUD(message: String,
                     content: Option[String],
                     sha: Option[String])

object GithubCUD {
  implicit val format: OFormat[GithubCUD] = Json.format[GithubCUD]
}
