package io.gitlab.scp2020.skyeng.domain.users.teacher

import cats.data.EitherT
import cats.{Functor, Monad}
import cats.syntax.functor._
import io.gitlab.scp2020.skyeng.domain.{
  TeacherAlreadyExistsError,
  TeacherNotFoundError
}

class TeacherProfileService[F[_]](
    teacherRepo: TeacherProfileRepositoryAlgebra[F],
    validation: TeacherProfileValidationAlgebra[F]
) {
  def createTeacher(teacherProfile: TeacherProfile)(implicit
      M: Monad[F]
  ): EitherT[F, TeacherAlreadyExistsError, TeacherProfile] =
    for {
      _ <- validation.teacherDoesNotExist(teacherProfile)
      saved <- EitherT.liftF(teacherRepo.create(teacherProfile))
    } yield saved

  def getTeacher(teacherId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, TeacherNotFoundError.type, TeacherProfile] =
    teacherRepo.get(teacherId).toRight(TeacherNotFoundError)

  def deleteTeacher(teacherId: Long)(implicit F: Functor[F]): F[Unit] =
    teacherRepo.delete(teacherId).value.void

  def updateTeacher(teacher: TeacherProfile)(implicit
      M: Monad[F]
  ): EitherT[F, TeacherNotFoundError.type, TeacherProfile] =
    for {
      _ <- validation.teacherExists(Some(teacher.userId))
      saved <- teacherRepo.update(teacher).toRight(TeacherNotFoundError)
    } yield saved
}

object TeacherProfileService {
  def apply[F[_]](
      teacherRepositoryAlgebra: TeacherProfileRepositoryAlgebra[F],
      validationAlgebra: TeacherProfileValidationAlgebra[F]
  ): TeacherProfileService[F] =
    new TeacherProfileService(teacherRepositoryAlgebra, validationAlgebra)
}
