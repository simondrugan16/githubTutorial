package model


import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.{Json, OFormat}

case class GithubCUDForm(login: String,
                         repoName: String,
                         path: String,
                         message: String,
                         content: Option[String],
                         sha: Option[String]
                        )

object GithubCUDForm {
  implicit val format: OFormat[GithubCUDForm] = Json.format[GithubCUDForm]
  val createFileForm: Form[GithubCUDForm] = Form(
    mapping(
      "login" -> text,
      "repoName" -> text,
      "path" -> text,
      "message" -> text,
      "content" -> optional(text),
      "sha" -> optional(text)
    )(GithubCUDForm.apply)(GithubCUDForm.unapply)
  )
}