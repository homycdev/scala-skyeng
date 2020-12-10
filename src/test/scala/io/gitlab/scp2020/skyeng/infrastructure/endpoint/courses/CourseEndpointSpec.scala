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
import io.gitlab.scp2020.skyeng.domain.courses.Course
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class CourseEndpointSpec
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
    val routes = Router(
      "/course" -> courseEndpoints,
      "/user" -> usersEndpoints
    ).orNotFound
    routes
  }

  implicit val courseEnc: EntityEncoder[IO, Course] = jsonEncoderOf
  implicit val courseDec: EntityDecoder[IO, Course] = jsonOf
  implicit val courseReqEnc: EntityEncoder[IO, CourseCreateRequest] =
    jsonEncoderOf
  implicit val courseReqDec: EntityDecoder[IO, CourseCreateRequest] = jsonOf

  test("Create course") {
    forAll { course: Course =>
      forAll { userSignup: SignupRequest =>
        (for {
          loginResp <- signUpAndLogInAsAdmin(userSignup, routes)
          (_, Some(authorization)) = loginResp
          courseCreateReq = CourseCreateRequest(
            title = course.title,
            categoryId = course.categoryId
          )
          createRequest <- POST(courseCreateReq, uri"/course")
          createRequestAuth = createRequest.putHeaders(authorization)
          createResponse <- routes.run(createRequestAuth)
          created <- createResponse.as[Course]
        } yield {
          createResponse.status shouldEqual Ok
          created.title shouldBe course.title
        }).unsafeRunSync()

      }

    }
  }
}
