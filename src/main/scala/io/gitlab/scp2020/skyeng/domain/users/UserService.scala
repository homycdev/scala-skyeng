package io.gitlab.scp2020.skyeng.domain.users

import cats.data._
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  UserAlreadyExistsError,
  UserNotFoundError
}

class UserService[F[_]](
    userRepo: UserRepositoryAlgebra[F],
    validation: UserValidationAlgebra[F]
) {
  def createUser(
      user: User
  )(implicit M: Monad[F]): EitherT[F, UserAlreadyExistsError, User] =
    for {
      _ <- validation.doesNotExist(user)
      saved <- EitherT.liftF(userRepo.create(user))
    } yield saved

  def getUser(
      userId: Long
  )(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User] =
    userRepo.get(userId).toRight(UserNotFoundError)

  def getUserByName(
      userName: String
  )(implicit F: Functor[F]): EitherT[F, UserNotFoundError.type, User] =
    userRepo.findByUserName(userName).toRight(UserNotFoundError)

  def deleteUser(userId: Long)(implicit F: Functor[F]): F[Unit] =
    userRepo.delete(userId).value.void

  def deleteByUserName(userName: String)(implicit F: Functor[F]): F[Unit] =
    userRepo.deleteByUserName(userName).value.void

  def update(
      user: User
  )(implicit M: Monad[F]): EitherT[F, UserNotFoundError.type, User] =
    for {
      _ <- validation.exists(user.id)
      saved <- userRepo.update(user).toRight(UserNotFoundError)
    } yield saved

  def list(pageSize: Int, offset: Int): F[List[User]] =
    userRepo.list(pageSize, offset)
}

object UserService {
  def apply[F[_]](
      userRepo: UserRepositoryAlgebra[F],
      validation: UserValidationAlgebra[F]
  ): UserService[F] =
    new UserService(userRepo, validation)
}
