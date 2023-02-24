package controllers

import model.User

import javax.inject._
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import service.UserService

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class FakeGithubTutorialController @Inject()(val controllerComponents: ControllerComponents, implicit val ec: ExecutionContext, userService: UserService) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def getGithubUser(username: String): Action[AnyContent] = Action.async { implicit request =>
    userService.getGithubUser(username = username).value.map {
      case Right(user: User) => Ok(Json.toJson(user))
      case Left(error) => InternalServerError
    }
  }

}
