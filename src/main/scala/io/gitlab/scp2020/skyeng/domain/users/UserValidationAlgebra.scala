package io.gitlab.scp2020.skyeng.domain.users

import cats.data.EitherT
import io.gitlab.scp2020.skyeng.domain.{UserAlreadyExistsError, UserNotFoundError}

trait UserValidationAlgebra[F[_]] {
  def doesNotExist(user: User): EitherT[F, UserAlreadyExistsError, Unit]

  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, Unit]
}