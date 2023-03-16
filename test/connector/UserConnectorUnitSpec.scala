package connector

import baseSpec.BaseSpec
import com.github.tomakehurst.wiremock._
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, getRequestedFor, stubFor, urlPathEqualTo}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import model.User
import model.User.formats
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Injecting

import scala.concurrent.ExecutionContext

class UserConnectorUnitSpec extends BaseSpec with Injecting with GuiceOneAppPerSuite {

//  private val port = 8080
//  private val hostname = "localhost"
//  // Run wiremock server on local machine with specified port.
//  private val wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port))
//
//  implicit val ws: WSClient = app.injector.instanceOf[WSClient]
//  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
//
//  def beforeEach {
//    wireMockServer.start()
//    WireMock.configureFor("localhost", 8080)
//  }
//
//  def afterEach {
//    wireMockServer.stop()
//  }
//
//  val validatedResponse: User = User(login = "simondrugan16", created_at = "2022-11-29T10:30:35Z", location = null, followers = 0, following = 0)
//
//  val response: String =
//    """{
//      |  "login": "simondrugan16",
//      |  "id": 119412400,
//      |  "node_id": "U_kgDOBx4WsA",
//      |  "avatar_url": "https://avatars.githubusercontent.com/u/119412400?v=4",
//      |  "gravatar_id": "",
//      |  "url": "https://api.github.com/users/simondrugan16",
//      |  "html_url": "https://github.com/simondrugan16",
//      |  "followers_url": "https://api.github.com/users/simondrugan16/followers",
//      |  "following_url": "https://api.github.com/users/simondrugan16/following{/other_user}",
//      |  "gists_url": "https://api.github.com/users/simondrugan16/gists{/gist_id}",
//      |  "starred_url": "https://api.github.com/users/simondrugan16/starred{/owner}{/repo}",
//      |  "subscriptions_url": "https://api.github.com/users/simondrugan16/subscriptions",
//      |  "organizations_url": "https://api.github.com/users/simondrugan16/orgs",
//      |  "repos_url": "https://api.github.com/users/simondrugan16/repos",
//      |  "events_url": "https://api.github.com/users/simondrugan16/events{/privacy}",
//      |  "received_events_url": "https://api.github.com/users/simondrugan16/received_events",
//      |  "type": "User",
//      |  "site_admin": false,
//      |  "name": null,
//      |  "company": null,
//      |  "blog": "",
//      |  "location": null,
//      |  "email": null,
//      |  "hireable": null,
//      |  "bio": null,
//      |  "twitter_username": null,
//      |  "public_repos": 4,
//      |  "public_gists": 0,
//      |  "followers": 0,
//      |  "following": 0,
//      |  "created_at": "2022-11-29T10:30:35Z",
//      |  "updated_at": "2022-12-15T10:36:58Z"
//      |}""".stripMargin

//  "UserConnector .get" should {
//    "Make a GET request to the API and return the response" in {
//
//      val path = s"/github/users/simondrugan16/"
//      stubFor(
//        WireMock.get(urlPathEqualTo(path))
//          .willReturn(aResponse()
//            .withStatus(200)
//            .withBody(response)
//          )
//      )
//
//      val connector: UserConnector = new UserConnector(ws)
//
//      val result = connector.get("/github/users/:username/").value.map {
//        case Left(value) => fail(s"This failed with unexpected value $value")
//        case Right(value) => value shouldBe validatedResponse
//      }
//    }
//  }
}
