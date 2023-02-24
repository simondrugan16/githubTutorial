package service

import cats.data.EitherT
import connector.UserConnector
import model.User
import play.api.mvc.Result

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(userConnector: UserConnector){

  def getGithubUser(urlOverride: Option[String] = None, username: String)(implicit ec: ExecutionContext): EitherT[Future, Result, User] =
    userConnector.get[User](urlOverride.getOrElse(s"https://api.github.com/users/$username"))
}
