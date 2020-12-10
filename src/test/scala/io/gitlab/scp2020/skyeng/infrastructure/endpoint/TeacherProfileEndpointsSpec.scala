package io.gitlab.scp2020.skyeng.infrastructure.endpoint

import cats.effect._
//import io.circe.generic.auto._
import io.gitlab.scp2020.skyeng.SkyEngArbitraries
import io.gitlab.scp2020.skyeng.configs.UsersModuleConfig
//import io.gitlab.scp2020.skyeng.domain.authentication._
//import io.gitlab.scp2020.skyeng.domain.users.teacher.TeacherProfile
import org.http4s._
//import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl._
import org.http4s.implicits._
import org.http4s.server.Router
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TeacherProfileEndpointsSpec
    extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with Http4sDsl[IO]
    with Http4sClientDsl[IO]
    with LoginTest
    with SkyEngArbitraries
    with UsersModuleConfig{

  def resources(): (
    HttpApp[IO],
  ) = {
    val routes = Router(
      "/user" -> usersEndpoints,
      "/teachers" -> teacherEndpoints
    ).orNotFound
    (routes)
  }

//  test("Set user to teacher"){
//    val routes = resources()
//    forAll{
//      ()
//    }
//  }


//  test("set user to teacher") {
//    val (routes) = resources()
//    forAll { (userSignup: SignupRequest, teacher: TeacherProfile) =>
//      (for {
//        loginResp <- signUpAndLogInAsAdmin(userSignup, routes)
//        (createdAdmin, authorization) = loginResp
//        userToSetTeacher = teacher.copy(userId = createdAdmin.id.get)
//        setToTeacher <- POST(
//          userToSetTeacher,
//          Uri.unsafeFromString(s"/teachers/assign/${createdAdmin.id.get}")
//        )
//        updatedUserAuth = setToTeacher.putHeaders(authorization.get)
//        setResponse <- routes.run(updatedUserAuth)
//        newTeacher <- setResponse.as[TeacherProfile]
//      } yield {
//        setResponse.status shouldEqual Ok
//        newTeacher.userId shouldEqual createdAdmin.id.get
//      }).unsafeRunSync()
//
//    }
//  }
}
