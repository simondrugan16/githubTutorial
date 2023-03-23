package service

import cats.data.EitherT
import connector.UserConnector
import model.{GithubFile, GithubFolderOrFile, GithubCUD, User, UserRepo}
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.Result

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(userConnector: UserConnector){

  def getGithubUser(urlOverride: Option[String] = None, login: String)(implicit ec: ExecutionContext): EitherT[Future, Result, User] =
    userConnector.get[User](urlOverride.getOrElse(s"https://api.github.com/users/$login"))

  def getGithubUserRepos(urlOverride: Option[String] = None, login: String)(implicit ec: ExecutionContext): EitherT[Future, Result, Seq[UserRepo]] =
    userConnector.get[Seq[UserRepo]](urlOverride.getOrElse(s"https://api.github.com/users/$login/repos"))

  def getGithubRepoFoldersAndFiles(urlOverride: Option[String] = None, login: String, repoName: String)
                                  (implicit ec: ExecutionContext): EitherT[Future, Result, Seq[GithubFolderOrFile]] =
    userConnector.get[Seq[GithubFolderOrFile]](urlOverride.getOrElse(s"https://api.github.com/repos/$login/$repoName/contents"))

  def getGithubRepoFolder(urlOverride: Option[String] = None, login: String, repoName: String, path: String)
                       (implicit ec: ExecutionContext): EitherT[Future, Result, Seq[GithubFolderOrFile]] =
    userConnector.get[Seq[GithubFolderOrFile]](urlOverride.getOrElse(s"https://api.github.com/repos/$login/$repoName/contents/$path"))

  def getGithubRepoFile(urlOverride: Option[String] = None, login: String, repoName: String, path: String)
                                 (implicit ec: ExecutionContext): EitherT[Future, Result, GithubFile] =
    userConnector.getFileContent[GithubFile](urlOverride.getOrElse(s"https://api.github.com/repos/$login/$repoName/contents/$path"))

  def githubFilePut(urlOverride: Option[String] = None, login: String, repoName: String, path: String, githubPut: GithubCUD)
                   (implicit ec: ExecutionContext): Future[Int] =
    userConnector.put[GithubCUD](urlOverride.getOrElse(s"https://api.github.com/repos/$login/$repoName/contents/$path"), githubPut)

  def githubFileDelete(urlOverride: Option[String] = None, login: String, repoName: String, path: String, githubDelete: GithubCUD)
                   (implicit ec: ExecutionContext): Future[Int] =
    userConnector.delete[GithubCUD](urlOverride.getOrElse(s"https://api.github.com/repos/$login/$repoName/contents/$path"), githubDelete)

}

