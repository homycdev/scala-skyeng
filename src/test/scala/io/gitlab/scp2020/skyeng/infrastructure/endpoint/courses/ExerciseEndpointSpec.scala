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
import io.gitlab.scp2020.skyeng.domain.courses.exercises.Exercise
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ExerciseEndpointSpec
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
      "/user" -> usersEndpoints,
      "/exercise" -> exerciseEndpoints
    ).orNotFound
    routes
  }

  implicit val exerciseEnc: EntityEncoder[IO, Exercise] = jsonEncoderOf
  implicit val exerciseDec: EntityDecoder[IO, Exercise] = jsonOf

  test("Create a exercise") {
    forAll { exercise: Exercise =>
      forAll { userSignup: SignupRequest =>
        (for {
          loginResp <- signUpAndLogInAsAdmin(userSignup, routes)
          (_, Some(authorization)) = loginResp
          createRequest <- POST(exercise, uri"/exercise")
          createRequestAuth = createRequest.putHeaders(authorization)
          createResponse <- routes.run(createRequestAuth)
          created <- createResponse.as[Exercise]
        } yield {
          createResponse.status shouldEqual Ok
          created.taskId shouldBe exercise.taskId
          created.exerciseType shouldBe exercise.exerciseType
          created.content shouldBe exercise.content
        }).unsafeRunSync()

      }

    }
  }
}
