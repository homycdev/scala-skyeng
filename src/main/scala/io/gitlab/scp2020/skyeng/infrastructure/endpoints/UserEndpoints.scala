package io.gitlab.scp2020.skyeng.infrastructure.endpoints

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.controllers.UserController
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  UserAlreadyExistsError,
  UserAuthenticationFailedError,
  UserNotFoundError
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoint.{
  AuthEndpoint,
  AuthService
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.Pagination.{
  OptionalOffsetMatcher,
  OptionalPageSizeMatcher
}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import tsec.authentication._
import tsec.jwt.algorithms.JWTMacAlgo

class UserEndpoints[F[_]: Sync, A, Auth: JWTMacAlgo](
    userController: UserController[F, A, Auth]
) extends Http4sDsl[F] {

  def endpoints(
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] =
      Auth.adminOnly {
        updateEndpoint
          .orElse(searchByNameEndpoint)
          .orElse(deleteUserEndpoint)
          .orElse(listEndpoint)
      }

    val unauthEndpoints =
      loginEndpoint(auth.authenticator) <+>
        signupEndpoint

    unauthEndpoints <+> auth.liftService(authEndpoints)
  }

  private def loginEndpoint(
      auth: Authenticator[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "login" =>
        val response = userController.login(req)

        response.value.flatMap {
          case Right((user, token)) => Ok(user.asJson).map(auth.embed(_, token))
          case Left(UserAuthenticationFailedError(name)) =>
            BadRequest(s"Authentication failed for user $name")
        }
    }

  private def signupEndpoint: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        val response = userController.signUp(req)
        response.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(UserAlreadyExistsError(existing)) =>
            Conflict(
              s"The user with user name ${existing.userName} already exists"
            )
        }
    }

  private def updateEndpoint: AuthEndpoint[F, Auth] = {
    case req @ PUT -> Root / name asAuthed _ =>
      val response = userController.update(req, name)
      response.flatMap {
        case Right(saved)            => Ok(saved.asJson)
        case Left(UserNotFoundError) => NotFound("User not found")
      }
  }

  private def listEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root :? OptionalPageSizeMatcher(
          pageSize
        ) :? OptionalOffsetMatcher(
          offset
        ) asAuthed _ =>
      for {
        retrieved <- userController.list(pageSize, offset)
        resp <- Ok(retrieved.asJson)
      } yield resp

  }

  private def searchByNameEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / userName asAuthed _ =>
      val response = userController.searchByName(userName)
      response.value.flatMap {
        case Right(found)            => Ok(found.asJson)
        case Left(UserNotFoundError) => NotFound("The user was not found")
      }

  }

  private def deleteUserEndpoint: AuthEndpoint[F, Auth] = {
    case DELETE -> Root / userName asAuthed _ =>
      for {
        _ <- userController.deleteUser(userName)
        resp <- Ok()
      } yield resp

  }
}

object UserEndpoints {
  def endpoints[F[_]: Sync, A, Auth: JWTMacAlgo](
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
      userController: UserController[F, A, Auth]
  ): HttpRoutes[F] =
    new UserEndpoints[F, A, Auth](userController)
      .endpoints(auth)
}
