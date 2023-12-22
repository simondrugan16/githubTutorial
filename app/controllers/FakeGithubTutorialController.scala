package controllers

import model.{GithubCUD, GithubCUDForm, GithubFile, GithubFolderOrFile, User, UserRepo}

import javax.inject._
import play.api._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import play.filters.csrf.CSRF
import service.{RepositoryService, UserService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FakeGithubTutorialController @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext, userService: UserService,
                                             val repositoryService: RepositoryService) extends BaseController with play.api.i18n.I18nSupport {

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
        case JsError(_) => Future(InternalServerError)
        case JsSuccess(validatedGithubPut, _) =>
          userService
            .githubFilePut(
              login = login,
              repoName = repoName,
              path = path,
              githubPut = validatedGithubPut
            ).map(result => Ok(result.json))
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
          .map(response => (response.status, response.body))
          .map {
            case (status, body) if status == 200 => Ok(body)
            case _ => InternalServerError("There has been an error")
          }
      }
    }
  }

  def accessToken(implicit request: Request[_]) = {
    CSRF.getToken
  }

  def upsertFile(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.upsertFileForm(GithubCUDForm.CUDFileForm))
  }

  def upsertFileForm(): Action[AnyContent] = Action.async { implicit request =>
    accessToken
    GithubCUDForm.CUDFileForm.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(formWithErrors.toString))
      },
      formData => {
        val githubUpsertData = Json.toJson(GithubCUD(formData.message, formData.content, None))
        githubUpsertData.validate[GithubCUD] match {
          case JsError(error) => Future(InternalServerError)
          case JsSuccess(validatedGithubUpsert, _) =>
            userService.githubFilePut(login = formData.login, repoName = formData.repoName, path = formData.path, githubPut = validatedGithubUpsert).map(response => (response.status, response.body)).map {
              case (status, body) => Ok(views.html.upsertFileStatus(status, body))
              case _ => InternalServerError("There's been an error.")
            }
        }
      }
    )
  }

  def updateFile(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.updateFileForm(GithubCUDForm.CUDFileForm))
  }

  def updateFileForm(): Action[AnyContent] = Action.async { implicit request =>
    accessToken
    GithubCUDForm.CUDFileForm.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(formWithErrors.toString))
      },
      formData => {
        val githubUpdateData = Json.toJson(GithubCUD(formData.message, formData.content, formData.sha))
        githubUpdateData.validate[GithubCUD] match {
          case JsError(error) => Future(InternalServerError)
          case JsSuccess(validatedGithubUpdate, _) =>
            userService.githubFilePut(login = formData.login, repoName = formData.repoName, path = formData.path, githubPut = validatedGithubUpdate).map(response => (response.status, response.body)).map {
              case (status, body) => Ok(views.html.updateFileStatus(status, body))
              case _ => InternalServerError("There's been an error.")
            }
        }
      }
    )
  }

  def deleteFile(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.deleteFileForm(GithubCUDForm.CUDFileForm))
  }

  def deleteFileForm(): Action[AnyContent] = Action.async { implicit request =>
    accessToken
    GithubCUDForm.CUDFileForm.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(formWithErrors.toString))
      },
      formData => {
        val githubDeleteData = Json.toJson(GithubCUD(formData.message, None, formData.sha))
        githubDeleteData.validate[GithubCUD] match {
          case JsError(error) => Future(InternalServerError)
          case JsSuccess(validatedGithubDelete, _) =>
            userService.githubFileDelete(login = formData.login, repoName = formData.repoName, path = formData.path, githubDelete = validatedGithubDelete).map(response => (response.status, response.body)).map {
              case (status, body) => Ok(views.html.deleteFileStatus(status, body))
              case _ => InternalServerError("There's been an error.")
            }
        }
      }
    )
  }

}


















