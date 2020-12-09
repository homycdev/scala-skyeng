package io.gitlab.scp2020.skyeng.domain.courses

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  EnrollmentAlreadyExistsError,
  EnrollmentNotFoundError
}

class EnrollmentService[F[_]](
    enrollmentRepositoryAlgebra: EnrollmentRepositoryAlgebra[F]
) {
  def createEnrollment(enrollment: Enrollment)(implicit
      M: Monad[F]
  ): EitherT[F, EnrollmentAlreadyExistsError, Enrollment] =
    for {
      saved <- EitherT.liftF(enrollmentRepositoryAlgebra.create(enrollment))
    } yield saved

  def getEnrollment(enrollmentId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, EnrollmentNotFoundError.type, Enrollment] =
    enrollmentRepositoryAlgebra
      .get(enrollmentId)
      .toRight(EnrollmentNotFoundError)

  def deleteEnrollment(enrollmentId: Long)(implicit F: Functor[F]): F[Unit] =
    enrollmentRepositoryAlgebra
      .delete(enrollmentId)
      .value
      .void

  def updateEnrollment(enrollment: Enrollment)(implicit
      M: Monad[F]
  ): EitherT[F, EnrollmentNotFoundError.type, Enrollment] =
    for {
      saved <-
        enrollmentRepositoryAlgebra
          .update(enrollment)
          .toRight(EnrollmentNotFoundError)
    } yield saved

  def listEnrollments(pageSize: Int, offset: Int): F[List[Enrollment]] =
    enrollmentRepositoryAlgebra.list(pageSize, offset)

  def getEnrollmentsByStudentId(studentId: Long): F[List[Enrollment]] =
    enrollmentRepositoryAlgebra.getByStudentId(studentId)

  def getEnrollmentsByCourseId(courseId: Long): F[List[Enrollment]] =
    enrollmentRepositoryAlgebra.getByCourseId(courseId)
}

object EnrollmentService {
  def apply[F[_]](
      enrollmentRepositoryAlgebra: EnrollmentRepositoryAlgebra[F]
  ): EnrollmentService[F] =
    new EnrollmentService(enrollmentRepositoryAlgebra)
}
