package connector

import cats.data.EitherT
import model.{GithubCUD, GithubFile}
import play.api.http.Status.{CONFLICT, CREATED, NOT_FOUND, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{Format, JsError, JsSuccess, Json, OFormat}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError

import java.util.Base64
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserConnector @Inject() (ws: WSClient) {
  def get[Response](url: String)(implicit rds: Format[Response], ec: ExecutionContext): EitherT[Future, Result, Response] = {
    val request = ws.url(url)
    val response = request.get()
    EitherT {
      response
        .map {
          result =>
            result.json.validate[Response] match {
              case JsSuccess(returnedResponse, _) => Right(returnedResponse)
              case JsError(errors) => Left(InternalServerError)
            }
        }
        .recover { case _: WSResponse =>
          println("Hit777777777777 .recover method")
          Left(InternalServerError)
        }
    }
  }

  def getFileContent[Response](url: String)(implicit rds: Format[Response], ec: ExecutionContext): EitherT[Future, Result, GithubFile] = {
    val request = ws.url(url)
    val response = request.get()
    EitherT {
      response
        .map {
          result =>
            result.json.validate[GithubFile] match {
              case JsSuccess(returnedGithubFile, _) =>
                val base64DecodedFile = Base64.getMimeDecoder.decode(returnedGithubFile.content).map(_.toChar).mkString
                Right(GithubFile(name = returnedGithubFile.name, `type` = returnedGithubFile.`type`, content = base64DecodedFile))
              case JsError(errors) => Left(InternalServerError)

            }
        }
        .recover { case _: WSResponse =>
          println("666666666666666")
          Left(InternalServerError)
        }
    }
  }

  def put[RequestBody](url: String, myModel: RequestBody)(implicit rds: Format[RequestBody], ec: ExecutionContext): Future[WSResponse] = {
    val request: WSRequest = ws.url(url)
      .addHttpHeaders(("Accept", "application/vnd.github+json"), ("Authorization", s"Bearer ${sys.env("AuthPassword")}"), ("X-GitHub-Api-Version", "2022-11-28"))
    val response: Future[WSResponse] = request.put(Json.toJson(myModel))
    response
  }

  def delete[RequestBody](url: String, myModel: RequestBody)(implicit rds: Format[RequestBody], ec: ExecutionContext): Future[WSResponse] = {
    val request = ws.url(url)
      .addHttpHeaders(("Accept", "application/vnd.github+json"), ("Authorization", s"Bearer ${sys.env("AuthPassword")}"), ("X-GitHub-Api-Version", "2022-11-28"))
      .withBody(Json.toJson(myModel))
    val response = request.delete()
    response
  }
}
