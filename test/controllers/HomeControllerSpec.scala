package controllers

import baseSpec.BaseSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.OK
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.{GET, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Helpers, Injecting}

class HomeControllerSpec extends BaseSpec with GuiceOneAppPerSuite with Injecting {

  val controllerComponents: ControllerComponents = Helpers.stubControllerComponents()

  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      val controller = new HomeController(controllerComponents)
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")

    }

    "render the index page from the application" in {
      val controller = inject[HomeController]
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")

    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")

    }
  }
}