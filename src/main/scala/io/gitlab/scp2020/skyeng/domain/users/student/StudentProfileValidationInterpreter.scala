package io.gitlab.scp2020.skyeng.domain.users.student

import cats.Applicative
import cats.data.EitherT
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import io.gitlab.scp2020.skyeng.domain.{
  StudentAlreadyExistsError,
  StudentNotFoundError
}

class StudentProfileValidationInterpreter[F[_]: Applicative](
    studentRepositoryAlgebra: StudentProfileRepositoryAlgebra[F]
) extends StudentProfileValidationAlgebra[F] {
  override def studentDoesNotExist(
      student: StudentProfile
  ): EitherT[F, StudentAlreadyExistsError, Unit] =
    studentRepositoryAlgebra
      .get(student.userId)
      .map(StudentAlreadyExistsError)
      .toLeft(())

  override def studentExists(
      studentId: Option[Long]
  ): EitherT[F, StudentNotFoundError.type, Unit] =
    studentId match {
      case Some(id) =>
        studentRepositoryAlgebra
          .get(id)
          .toRight(StudentNotFoundError)
          .void

      case None =>
        EitherT.left[Unit](StudentNotFoundError.pure[F])
    }
}

object StudentProfileValidationInterpreter {
  def apply[F[_]: Applicative](
      repositoryAlgebra: StudentProfileRepositoryAlgebra[F]
  ): StudentProfileValidationAlgebra[F] =
    new StudentProfileValidationInterpreter[F](repositoryAlgebra)
}
