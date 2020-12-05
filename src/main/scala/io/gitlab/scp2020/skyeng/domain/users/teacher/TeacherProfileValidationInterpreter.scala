package io.gitlab.scp2020.skyeng.domain.users.teacher

import cats.Applicative
import cats.data.EitherT
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import io.gitlab.scp2020.skyeng.domain.{
  TeacherAlreadyExistsError,
  TeacherNotFoundError
}

class TeacherProfileValidationInterpreter[F[_]: Applicative](
    teacherRepositoryAlgebra: TeacherProfileRepositoryAlgebra[F]
) extends TeacherProfileValidationAlgebra[F] {
  override def teacherDoesNotExist(
      teacherProfile: TeacherProfile
  ): EitherT[F, TeacherAlreadyExistsError, Unit] =
    teacherRepositoryAlgebra
      .get(teacherProfile.userId)
      .map(TeacherAlreadyExistsError)
      .toLeft(())

  // TODO review this getOrElse statement.

  override def teacherExists(
      teacherId: Option[Long]
  ): EitherT[F, TeacherNotFoundError.type, Unit] =
    teacherId match {
      case Some(id) =>
        teacherRepositoryAlgebra
          .get(id)
          .toRight(TeacherNotFoundError)
          .void
      case None =>
        EitherT.left[Unit](TeacherNotFoundError.pure[F])
    }
}

object TeacherProfileValidationInterpreter {
  def apply[F[_]: Applicative](
      teacherRepositoryAlgebra: TeacherProfileRepositoryAlgebra[F]
  ): TeacherProfileValidationAlgebra[F] =
    new TeacherProfileValidationInterpreter[F](teacherRepositoryAlgebra)
}
