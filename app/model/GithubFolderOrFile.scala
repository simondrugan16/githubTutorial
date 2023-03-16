package model

import play.api.libs.json.{Json, OFormat}

case class GithubFolderOrFile(name: String,
                              path: String,
                              `type`: String)

object GithubFolderOrFile {
  implicit val format: OFormat[GithubFolderOrFile] = Json.format[GithubFolderOrFile]
}

