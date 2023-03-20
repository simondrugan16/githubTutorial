package connector

import baseSpec.BaseSpec
import com.github.tomakehurst.wiremock._
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, ok}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.Fault
import model.{GithubFile, User}
import model.User.formats
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.ws.WSClient
import play.api.test.Injecting

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.xml.Elem

class UserConnectorUnitSpec extends BaseSpec with Injecting with GuiceOneAppPerSuite {

  implicit val ws: WSClient = app.injector.instanceOf[WSClient]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(9000))

  val validatedResponse: User = User(
    login = "simondrugan16",
    created_at = "2022-11-29T10:30:35Z",
    location = None,
    followers = 0,
    following = 0
  )

  val nonValidatableResponse: String =
    """{
      |  "can't": "validate"
      |}""".stripMargin


  val xmlResponse = 22

  val response: String =
    """{
      |  "login": "simondrugan16",
      |  "id": 119412400,
      |  "node_id": "U_kgDOBx4WsA",
      |  "avatar_url": "https://avatars.githubusercontent.com/u/119412400?v=4",
      |  "gravatar_id": "",
      |  "url": "https://api.github.com/users/simondrugan16",
      |  "html_url": "https://github.com/simondrugan16",
      |  "followers_url": "https://api.github.com/users/simondrugan16/followers",
      |  "following_url": "https://api.github.com/users/simondrugan16/following{/other_user}",
      |  "gists_url": "https://api.github.com/users/simondrugan16/gists{/gist_id}",
      |  "starred_url": "https://api.github.com/users/simondrugan16/starred{/owner}{/repo}",
      |  "subscriptions_url": "https://api.github.com/users/simondrugan16/subscriptions",
      |  "organizations_url": "https://api.github.com/users/simondrugan16/orgs",
      |  "repos_url": "https://api.github.com/users/simondrugan16/repos",
      |  "events_url": "https://api.github.com/users/simondrugan16/events{/privacy}",
      |  "received_events_url": "https://api.github.com/users/simondrugan16/received_events",
      |  "type": "User",
      |  "site_admin": false,
      |  "name": null,
      |  "company": null,
      |  "blog": "",
      |  "location": null,
      |  "email": null,
      |  "hireable": null,
      |  "bio": null,
      |  "twitter_username": null,
      |  "public_repos": 4,
      |  "public_gists": 0,
      |  "followers": 0,
      |  "following": 0,
      |  "created_at": "2022-11-29T10:30:35Z",
      |  "updated_at": "2022-12-15T10:36:58Z"
      |}""".stripMargin

  val fileResponse: String =
    """{
    "name": ".gitignore",
    "path": ".gitignore",
    "sha": "dce73038415f7e68f232c49cefe21ad58a9056e7",
    "size": 86,
    "url": "https://api.github.com/repos/simondrugan16/githubTutorial/contents/.gitignore?ref=main",
    "html_url": "https://github.com/simondrugan16/githubTutorial/blob/main/.gitignore",
    "git_url": "https://api.github.com/repos/simondrugan16/githubTutorial/git/blobs/dce73038415f7e68f232c49cefe21ad58a9056e7",
    "download_url": "https://raw.githubusercontent.com/simondrugan16/githubTutorial/main/.gitignore",
    "type": "file",
    "content": "bG9ncwp0YXJnZXQKLy5ic3AKLy5pZGVhCi8uaWRlYV9tb2R1bGVzCi8uY2xh\nc3NwYXRoCi8ucHJvamVjdAovLnNldHRpbmdzCi9SVU5OSU5HX1BJRAo=\n",
    "encoding": "base64",
    "_links": {
      "self": "https://api.github.com/repos/simondrugan16/githubTutorial/contents/.gitignore?ref=main",
      "git": "https://api.github.com/repos/simondrugan16/githubTutorial/git/blobs/dce73038415f7e68f232c49cefe21ad58a9056e7",
      "html": "https://github.com/simondrugan16/githubTutorial/blob/main/.gitignore"
    }
  }"""

  val validatedGithubFile: GithubFile = GithubFile(
    name = ".gitignore",
    `type` = "file",
    content = "logs\ntarget\n/.bsp\n/.idea\n/.idea_modules\n/.classpath\n/.project\n/.settings\n/RUNNING_PID\n")

  "UserConnector .get" should {
    "Make a GET request to the API and return the validated response" in {

      val path = "http://localhost:9000/github/users/simondrugan16/"

      val connector: UserConnector = new UserConnector(ws)

      wireMockServer.start()

      wireMockServer.stubFor(get("/github/users/simondrugan16/")
        .willReturn(ok()
          .withHeader("Content-Type", "text/html; charset=UTF-8")
          .withBody(response)))

      Await.result(connector.get(path).value.map {
        case Left(value) => fail(s"This failed with unexpected value $value")
        case Right(value) => value shouldBe validatedResponse
      }, 2.minute)

      wireMockServer.stop()

    }

    "Make a GET request to the API and get a json validation error" in {

      val path = "http://localhost:9000/github/users/simondrugan16/"

      val connector: UserConnector = new UserConnector(ws)

      wireMockServer.start()

      wireMockServer.stubFor(get("/github/users/simondrugan16/")
        .willReturn(ok()
          .withHeader("Content-Type", "text/html; charset=UTF-8")
          .withBody(nonValidatableResponse)))

      Await.result(connector.get(path).value.map {
        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
        case Right(value) => fail(s"This incorrectly passed with unexpected value $value")
      }, 2.minute)

      wireMockServer.stop()

    }

//    "Make a GET request to the API, provide an incorrect URL and hit the .recover method within connector.getSD" in {
//
//      val path = "http://localhost:9000/incorrect/path/"
//
//      val connector: UserConnector = new UserConnector(ws)
//
//      wireMockServer.start()
//
//      wireMockServer.stubFor(get("/incorrect/path/")
//        .willReturn(aResponse()
//          .withFault(Fault.EMPTY_RESPONSE)))
//
//      Await.result(connector.get(path).value.map {
//        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
//        case Right(value) => fail(s"This incorrectly passed with unexpected value $value")
//      }, 2.minute)
//
//      wireMockServer.stop()
//    }
  }
  "UserConnector .getFileContent" should {
    "Make a GET request to the API and return the validated response" in {

      val path = "http://localhost:9000/github/users/simondrugan16/repos/githubTutorial/file/.gitignore"

      val connector: UserConnector = new UserConnector(ws)

      wireMockServer.start()

      wireMockServer.stubFor(get("/github/users/simondrugan16/repos/githubTutorial/file/.gitignore")
        .willReturn(ok()
          .withHeader("Content-Type", "text/html; charset=UTF-8")
          .withBody(fileResponse)))

      Await.result(connector.getFileContent(path).value.map {
        case Left(value) => fail(s"This failed with unexpected value $value")
        case Right(value) => value shouldBe validatedGithubFile
      }, 2.minute)

      wireMockServer.stop()

    }

    "Make a GET request to the API and get a json validation error" in {

      val path = "http://localhost:9000/github/users/simondrugan16/repos/githubTutorial/file/.gitignore"

      val connector: UserConnector = new UserConnector(ws)

      wireMockServer.start()

      wireMockServer.stubFor(get("/github/users/simondrugan16/repos/githubTutorial/file/.gitignore")
        .willReturn(ok()
          .withHeader("Content-Type", "text/html; charset=UTF-8")
          .withBody(nonValidatableResponse)))

      Await.result(connector.getFileContent(path).value.map {
        case Left(value) => value.header.status shouldBe Status.INTERNAL_SERVER_ERROR
        case Right(value) => fail(s"This incorrectly passed with unexpected value $value")
      }, 2.minute)

      wireMockServer.stop()

    }
  }
}
