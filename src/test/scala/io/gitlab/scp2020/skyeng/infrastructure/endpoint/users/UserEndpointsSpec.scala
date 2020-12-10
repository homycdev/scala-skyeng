package io.gitlab.scp2020.skyeng.infrastructure.endpoint.users

import _root_.io.gitlab.scp2020.skyeng.SkyEngArbitraries._
import _root_.io.gitlab.scp2020.skyeng.configs.UsersModuleConfig
import _root_.io.gitlab.scp2020.skyeng.domain.authentication._
import _root_.io.gitlab.scp2020.skyeng.domain.users._
import _root_.io.gitlab.scp2020.skyeng.infrastructure.endpoint.LoginTest
import cats.effect._
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl._
import org.http4s.implicits._
import org.http4s.server.Router
import org.scalatest.enablers.Definition.definitionOfOption
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UserEndpointsSpec
    extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with Http4sDsl[IO]
    with Http4sClientDsl[IO]
    with UsersModuleConfig
    with LoginTest {
  def userRoutes(): HttpApp[IO] = {
    Router(("/user", usersEndpoints)).orNotFound
  }

  test("create user and log in") {
    val userEndpoint = userRoutes()

    forAll { userSignup: SignupRequest =>
      val (_, authorization) =
        signUpAndLogIn(userSignup, userEndpoint).unsafeRunSync()
      authorization shouldBe defined
    }
  }

  test("update user") {
    val userEndpoint = userRoutes()

    forAll { userSignup: SignupRequest =>
      (for {
        loginResp <- signUpAndLogInAsAdmin(userSignup, userEndpoint)
        (createdUser, authorization) = loginResp
        userToUpdate =
          createdUser.copy(lastName = Some(createdUser.lastName.get.reverse))
        updateUser <- PUT(
          userToUpdate,
          Uri.unsafeFromString(s"/user/${createdUser.userName}")
        )
        updateUserAuth = updateUser.putHeaders(authorization.get)
        updateResponse <- userEndpoint.run(updateUserAuth)
        updatedUser <- updateResponse.as[User]
      } yield {
        updateResponse.status shouldEqual Ok
        updatedUser.lastName shouldEqual Some(createdUser.lastName.get.reverse)
        createdUser.id shouldEqual updatedUser.id
      }).unsafeRunSync()
    }
  }

  test("get user by userName") {
    val userEndpoint = userRoutes()

    forAll { userSignup: SignupRequest =>
      (for {
        loginResp <- signUpAndLogInAsAdmin(userSignup, userEndpoint)
        (createdUser, authorization) = loginResp
        getRequest <-
          GET(Uri.unsafeFromString(s"/user/${createdUser.userName}"))
        getRequestAuth = getRequest.putHeaders(authorization.get)
        getResponse <- userEndpoint.run(getRequestAuth)
        getUser <- getResponse.as[User]
      } yield {
        getResponse.status shouldEqual Ok
        createdUser.userName shouldEqual getUser.userName
      }).unsafeRunSync()
    }
  }

  test("delete user by userName") {
    val userEndpoint = userRoutes()

    forAll { userSignup: SignupRequest =>
      (for {
        loginResp <- signUpAndLogInAsStudent(userSignup, userEndpoint)
        (createdUser, Some(authorization)) = loginResp
        deleteRequest <-
          DELETE(Uri.unsafeFromString(s"/user/${createdUser.userName}"))
        deleteRequestAuth = deleteRequest.putHeaders(authorization)
        deleteResponse <- userEndpoint.run(deleteRequestAuth)
      } yield deleteResponse.status shouldEqual Unauthorized).unsafeRunSync()
    }

    forAll { userSignup: SignupRequest =>
      (for {
        loginResp <- signUpAndLogInAsAdmin(userSignup, userEndpoint)
        (createdUser, Some(authorization)) = loginResp
        deleteRequest <-
          DELETE(Uri.unsafeFromString(s"/user/${createdUser.userName}"))
        deleteRequestAuth = deleteRequest.putHeaders(authorization)
        deleteResponse <- userEndpoint.run(deleteRequestAuth)
        getRequest <-
          GET(Uri.unsafeFromString(s"/user/${createdUser.userName}"))
        getRequestAuth = getRequest.putHeaders(authorization)
        getResponse <- userEndpoint.run(getRequestAuth)
      } yield {
        deleteResponse.status shouldEqual Ok
        // The user not the token longer exist
        getResponse.status shouldEqual Unauthorized
      }).unsafeRunSync()
    }
  }
}
