package io.gitlab.scp2020.skyeng.domain.users.student

import cats.data.EitherT
import io.gitlab.scp2020.skyeng.domain.{
  StudentAlreadyExistsError,
  StudentNotFoundError
}

trait StudentProfileValidationAlgebra[F[_]] {
  def studentDoesNotExist(
      student: StudentProfile
  ): EitherT[F, StudentAlreadyExistsError, Unit]

  def studentExists(
      studentId: Option[Long]
  ): EitherT[F, StudentNotFoundError.type, Unit]
}
