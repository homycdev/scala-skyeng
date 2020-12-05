package io.gitlab.scp2020.skyeng.domain.users.teacher

import cats.data.EitherT
import io.gitlab.scp2020.skyeng.domain.{
  TeacherAlreadyExistsError,
  TeacherNotFoundError
}

trait TeacherProfileValidationAlgebra[F[_]] {
  def teacherDoesNotExist(
      teacherProfile: TeacherProfile
  ): EitherT[F, TeacherAlreadyExistsError, Unit]

  def teacherExists(
      teacherId: Option[Long]
  ): EitherT[F, TeacherNotFoundError.type, Unit]
}
