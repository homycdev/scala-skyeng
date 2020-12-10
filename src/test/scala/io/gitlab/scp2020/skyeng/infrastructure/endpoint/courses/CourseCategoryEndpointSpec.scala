package io.gitlab.scp2020.skyeng.infrastructure.endpoint.courses

import _root_.io.gitlab.scp2020.skyeng.configs.{
  CoursesModuleConfig,
  UsersModuleConfig
}
import _root_.io.gitlab.scp2020.skyeng.domain.authentication._
import _root_.io.gitlab.scp2020.skyeng.infrastructure.endpoint.LoginTest
import cats.effect.IO
import io.circe.generic.auto._
import io.gitlab.scp2020.skyeng.SkyEngArbitraries
import io.gitlab.scp2020.skyeng.domain.courses.CourseCategory
import io.gitlab.scp2020.skyeng.domain.users.Role
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class CourseCategoryEndpointSpec
    extends AnyFunSuite
    with CoursesModuleConfig
    with UsersModuleConfig
    with Matchers
    with ScalaCheckPropertyChecks
    with Http4sDsl[IO]
    with Http4sClientDsl[IO]
    with LoginTest
    with SkyEngArbitraries {

  def routes: HttpApp[IO] = {
    Router(
      "/course_category" -> courseCategoryEndpoints,
      "/user" -> usersEndpoints
    ).orNotFound
  }

  implicit val categoryEnc: EntityEncoder[IO, CourseCategory] = jsonEncoderOf
  implicit val categoryDec: EntityDecoder[IO, CourseCategory] = jsonOf
  implicit val courseCategoryEnc: EntityEncoder[IO, CourseCategoryRequest] =
    jsonEncoderOf
  implicit val courseCategoryDec: EntityDecoder[IO, CourseCategoryRequest] =
    jsonOf

  test("Create course category") {
    forAll { category: CourseCategory =>
      forAll { userSignup: SignupRequest =>
        (for {
          loginResp <- signUpAndLogInAsAdmin(userSignup, routes)
          (user, Some(authorization)) = loginResp
          categoryRequest = CourseCategoryRequest(title = category.title)
          createRequest <- POST(categoryRequest, uri"/course_category")
          createRequestAuth = createRequest.putHeaders(authorization)
          createResponse <- routes.run(createRequestAuth)
          created <- createResponse.as[CourseCategory]
        } yield {
          createRequest.method shouldEqual POST
          createRequestAuth.method shouldEqual POST
          user.role shouldEqual Role.Admin
          createResponse.status shouldEqual Ok
          created.title shouldBe category.title
        }).unsafeRunSync()

      }

    }
  }
}
