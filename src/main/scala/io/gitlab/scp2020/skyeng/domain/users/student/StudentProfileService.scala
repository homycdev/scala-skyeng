package io.gitlab.scp2020.skyeng.domain.users.student

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  StudentAlreadyExistsError,
  StudentNotFoundError
}

class StudentProfileService[F[_]](
    studentRepositoryAlgebra: StudentProfileRepositoryAlgebra[F],
    studentValidationAlgebra: StudentProfileValidationAlgebra[F]
) {
  def createStudent(student: StudentProfile)(implicit
      M: Monad[F]
  ): EitherT[F, StudentAlreadyExistsError, StudentProfile] =
    for {
      _ <- studentValidationAlgebra.studentDoesNotExist(student)
      saved <- EitherT.liftF(studentRepositoryAlgebra.create(student))
    } yield saved

  def getStudent(studentId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, StudentNotFoundError.type, StudentProfile] =
    studentRepositoryAlgebra
      .get(studentId)
      .toRight(StudentNotFoundError)

  def deleteStudent(studentId: Long)(implicit F: Functor[F]): F[Unit] =
    studentRepositoryAlgebra
      .delete(studentId)
      .value
      .void

  def updateStudent(student: StudentProfile)(implicit
      M: Monad[F]
  ): EitherT[F, StudentNotFoundError.type, StudentProfile] =
    for {
      _ <- studentValidationAlgebra.studentExists(Some(student.userId))
      saved <-
        studentRepositoryAlgebra.update(student).toRight(StudentNotFoundError)
    } yield saved

}

object StudentProfileService {
  def apply[F[_]](
      studentRepositoryAlgebra: StudentProfileRepositoryAlgebra[F],
      studentValidationAlgebra: StudentProfileValidationAlgebra[F]
  ): StudentProfileService[F] =
    new StudentProfileService(
      studentRepositoryAlgebra,
      studentValidationAlgebra
    )
}
