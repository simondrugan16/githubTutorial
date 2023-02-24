package connector

import cats.data.EitherT
import model.User
import play.api.libs.json.{JsError, JsSuccess, OFormat}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserConnector @Inject() (ws: WSClient) {
  def get[Response](url: String)(implicit rds: OFormat[Response], ec: ExecutionContext): EitherT[Future, Result, User] = {
    val request = ws.url(url)
    val response = request.get()
    EitherT {
      response
        .map {
          result =>
            result.json.validate[User] match {
              case JsSuccess(returnedUser, _) => Right(User(
                returnedUser.login,
                returnedUser.created_at,
                returnedUser.location,
                returnedUser.followers,
                returnedUser.following))
              case JsError(errors) => {
                Left(InternalServerError)}
            }
        }
        .recover { case _: WSResponse =>
          Left(InternalServerError)
        }
    }
  }
}
