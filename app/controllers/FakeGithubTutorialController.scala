package controllers

import model.{GithubFile, GithubFolderOrFile, GithubCUD, User, UserRepo}

import javax.inject._
import play.api._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import service.{RepositoryService, UserService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FakeGithubTutorialController @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext, userService: UserService, val repositoryService: RepositoryService) extends BaseController {

  def validateEmpty(id: String): Either[Result, String] = {
    if (id.isEmpty)
      Left(BadRequest("ID empty"))
    else
      Right(id)
  }

  def validateEmptyThree(first: String, second: String, third: String): Either[Result, String] = {
    if (first.isEmpty || second.isEmpty || third.isEmpty)
      Left(BadRequest("ID Empty"))
    else
      Right("All good")
  }

  def getGithubUser(login: String): Action[AnyContent] = Action.async { implicit request =>
    userService.getGithubUser(login = login).value.map {
      case Right(user: User) => Ok(views.html.displayGithubUser(user))
      case Left(error) => InternalServerError
    }
  }

  def getGithubUserRepos(login: String): Action[AnyContent] = Action.async { implicit request =>
    userService.getGithubUserRepos(login = login).value.map {
      case Right(userRepos: Seq[UserRepo]) => Ok(views.html.displayGithubUsersRepos(userRepos))
      case Left(error) => InternalServerError
    }
  }

  def getGithubRepoFoldersAndFiles(login: String, repoName: String): Action[AnyContent] = Action.async {implicit request =>
    userService.getGithubRepoFoldersAndFiles(login = login, repoName = repoName).value.map {
      case Right(foldersAndFiles: Seq[GithubFolderOrFile]) => Ok(views.html.displayGithubRepoFoldersAndFiles(foldersAndFiles, login, repoName))
      case Left(error) => InternalServerError
    }
  }

  def getGithubRepoFolder(login: String, repoName: String, path: String): Action[AnyContent] = Action.async { implicit request =>
    userService.getGithubRepoFolder(login = login, repoName = repoName, path = path).value.map {
      case Right(foldersAndFiles: Seq[GithubFolderOrFile]) =>
        Ok(views.html.displayGithubRepoFoldersAndFiles(foldersAndFiles, login, repoName))
      case Left(error) => InternalServerError
    }
  }

  def getGithubRepoFile(login: String, repoName: String, path: String): Action[AnyContent] = Action.async { implicit request =>
    userService.getGithubRepoFile(login = login, repoName = repoName, path = path).value.map {
      case Right(file: GithubFile) => Ok(views.html.displayGithubRepoFile(file, login, repoName))
      case Left(error) => InternalServerError
    }
  }

  def addGithubUserToDatabase(login: String): Action[AnyContent] = Action.async { implicit request =>
    userService.getGithubUser(login = login).value.flatMap {
      case Left(error) => Future(InternalServerError)
      case Right(user) => repositoryService.create(user).map {
        case Left(error) => error
        case Right(user) => Created
      }
    }
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[User] match {
      case JsSuccess(user, _) =>
        repositoryService.create(user).map {
          case Left(error) => error
          case Right(result) => Created
        }
      case JsError(_) => Future(BadRequest)
    }
  }

  def read(login: String): Action[AnyContent] = Action.async { implicit request =>
    validateEmpty(login) match {
      case Left(error) => Future(error)
      case Right(validatedLogin) =>
        for {
          user <- repositoryService.read(validatedLogin)
          res = user match {
            case Right(user) => Ok(Json.toJson(user))
            case Left(error) => error
          }} yield
          res
    }
  }

  def update(login: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[User]
    match {
      case JsSuccess(user, _) =>
        repositoryService.update(login, user).map {
          case Left(error) => error
          case Right(result) => Accepted
        }
      case JsError(_) => Future(BadRequest)
    }
  }

  def delete(login: String): Action[AnyContent] = Action.async { implicit request =>
    validateEmpty(login) match {
      case Left(error) => Future(error)
      case Right(validatedLogin) =>
        for {
          user <- repositoryService.delete(validatedLogin)
          res = user match {
            case Right(user) => Accepted
            case Left(error) => error
          }} yield
          res
    }
  }

  def githubFilePut(login: String, repoName: String, path: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    validateEmptyThree(login, repoName, path) match {
      case Left(error) => Future(error)
      case Right(_) => request.body.validate[GithubCUD] match {
        case JsError(error) => Future(InternalServerError)
        case JsSuccess(validatedGithubPut, _) => userService.
          githubFilePut(login = login, repoName = repoName, path = path, githubPut = validatedGithubPut)
          .map {
          case 200 => Ok
          case 201 => Created
          case 404 => NotFound
          case 409 => Conflict
          case 422 => UnprocessableEntity
        }
      }
    }
  }

  def githubFileDelete(login: String, repoName: String, path: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    validateEmptyThree(login, repoName, path) match {
      case Left(error) => Future(error)
      case Right(_) => request.body.validate[GithubCUD] match {
        case JsError(error) => Future(InternalServerError)
        case JsSuccess(validatedGithubDelete, _) => userService.
          githubFileDelete(login = login, repoName = repoName, path = path, githubDelete = validatedGithubDelete)
          .map {
            case 200 => Ok
            case 404 => NotFound
            case 409 => Conflict
            case 422 => UnprocessableEntity
            case 503 => ServiceUnavailable
          }
      }
    }
  }
}
