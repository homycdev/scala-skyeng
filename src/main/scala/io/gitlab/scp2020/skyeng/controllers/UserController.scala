package io.gitlab.scp2020.skyeng.controllers

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.gitlab.scp2020.skyeng.domain.authentication.{
  LoginRequest,
  SignupRequest,
  UpdateRequest
}
import io.gitlab.scp2020.skyeng.domain.users.{User, UserService}
import io.gitlab.scp2020.skyeng.domain.{
  UserAlreadyExistsError,
  UserAuthenticationFailedError,
  UserNotFoundError
}
import org.http4s.circe._
import org.http4s.{EntityDecoder, _}
import tsec.authentication._
import tsec.common.Verified
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

class UserController[F[_]: Sync, A, Auth: JWTMacAlgo](
    userService: UserService[F],
    cryptService: PasswordHasher[F, A],
    auth: Authenticator[F, Long, User, AugmentedJWT[Auth, Long]]
) {
  /* Jsonization of our User type */
  implicit val userDecoder: EntityDecoder[F, User] = jsonOf
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequest] = jsonOf
  implicit val signupReqDecoder: EntityDecoder[F, SignupRequest] = jsonOf
  implicit val updateReqDec: EntityDecoder[F, UpdateRequest] = jsonOf

  def login(
      request: Request[F]
  ): EitherT[
    F,
    UserAuthenticationFailedError,
    (User, AugmentedJWT[Auth, Long])
  ] = {
    val action = for {
      login <- EitherT.liftF(request.as[LoginRequest])
      name = login.userName
      user <-
        userService
          .getUserByName(name)
          .leftMap(_ => UserAuthenticationFailedError(name))
      checkResult <- EitherT.liftF(
        cryptService.checkpw(login.password, PasswordHash[A](user.hash))
      )
      _ <-
        if (checkResult == Verified)
          EitherT.rightT[F, UserAuthenticationFailedError](())
        else EitherT.leftT[F, User](UserAuthenticationFailedError(name))
      token <- user.id match {
        case None =>
          throw new Exception("Impossible") // User is not properly modeled
        case Some(id) =>
          EitherT.right[UserAuthenticationFailedError](auth.create(id))
      }
    } yield (user, token)

    action
  }

  def signUp(
      request: Request[F]
  ): F[Either[UserAlreadyExistsError, User]] = {
    val action = for {
      signup <- request.as[SignupRequest]
      hash <- cryptService.hashpw(signup.password)
      user <- signup.asUser(hash).pure[F]
      result <- userService.createUser(user).value
    } yield result

    action
  }

  def update(
      request: SecuredRequest[F, User, AugmentedJWT[Auth, Long]],
      name: String
  ): F[Either[UserNotFoundError.type, User]] = {
    val findUser = for {
      user <- userService.getUserByName(name).value
    } yield user

    findUser.flatMap {
      case Right(user) =>
        for {
          req <- request.request.as[UpdateRequest]
          updatable = req.asUser(user)
          result <- userService.update(updatable).value
        } yield result
      case Left(UserNotFoundError) =>
        findUser
    }
  }

  def list(
      pageSize: Option[Int],
      offset: Option[Int]
  ): F[List[User]] =
    userService.list(pageSize.getOrElse(10), offset.getOrElse(0))

  def searchByName(
      userName: String
  ): EitherT[F, UserNotFoundError.type, User] = {
    userService.getUserByName(userName)
  }

  def deleteUser(
      userName: String
  ): F[Unit] = userService.deleteByUserName(userName)

}

object UserController {
  def apply[F[_]: Sync, A, Auth: JWTMacAlgo](
      userService: UserService[F],
      cryptService: PasswordHasher[F, A],
      auth: Authenticator[F, Long, User, AugmentedJWT[Auth, Long]]
  ): UserController[F, A, Auth] =
    new UserController[F, A, Auth](userService, cryptService, auth)
}
