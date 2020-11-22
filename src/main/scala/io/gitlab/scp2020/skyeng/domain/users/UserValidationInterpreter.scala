package io.gitlab.scp2020.skyeng.domain.users

import cats.Applicative
import cats.data.EitherT
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import io.gitlab.scp2020.skyeng.domain.{UserAlreadyExistsError, UserNotFoundError}

class UserValidationInterpreter[F[_] : Applicative] (userRepo: UserRepositoryAlgebra[F]) extends UserValidationAlgebra[F]{
  override def doesNotExist(user: User): EitherT[F, UserAlreadyExistsError, Unit] =
    userRepo
      .findByUserName(user.userName)
      .map(UserAlreadyExistsError)
      .toLeft(())

  override def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, Unit] =
    userId match {
      case Some(id) =>
        userRepo
        .get(id)
        .toRight(UserNotFoundError)
        .void

      case None =>
        EitherT.left[Unit](UserNotFoundError.pure[F])
    }
}

object UserValidationInterpreter{
  def apply[F[_] : Applicative](userRepo: UserRepositoryAlgebra[F]): UserValidationAlgebra[F] =
    new UserValidationInterpreter(userRepo)
}
