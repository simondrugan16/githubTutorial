package connector

import cats.data.EitherT
import model.GithubFile
import play.api.libs.json.{Format, JsError, JsSuccess, OFormat}
import play.api.libs.ws.{WSClient, WSResponse}
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
              case JsError(errors) => {
                println(s"PPPPPPPPPPPPPPPPPPPP\n $errors")
                Left(InternalServerError)}
            }
        }
        .recover { case _: WSResponse =>
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
              case JsError(errors) => {
                println(s"PPPPPPPPPPPPPPPPPPPP\n $errors")
                Left(InternalServerError)
              }
            }
        }
        .recover { case _: WSResponse =>
          Left(InternalServerError)
        }
    }
  }
}
