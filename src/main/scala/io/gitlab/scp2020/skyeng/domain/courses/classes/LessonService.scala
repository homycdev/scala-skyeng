package io.gitlab.scp2020.skyeng.domain.courses.classes

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  LessonAlreadyExistsError,
  LessonNotFoundError
}

class LessonService[F[_]](
    lessonRepositoryAlgebra: LessonRepositoryAlgebra[F]
) {
  def createLesson(lesson: Lesson)(implicit
      M: Monad[F]
  ): EitherT[F, LessonAlreadyExistsError, Lesson] =
    for {
      saved <- EitherT.liftF(lessonRepositoryAlgebra.create(lesson))
    } yield saved

  def getLesson(classId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, LessonNotFoundError.type, Lesson] =
    lessonRepositoryAlgebra
      .get(classId)
      .toRight(LessonNotFoundError)

  def deleteLesson(
      classId: Long
  )(implicit F: Functor[F]): F[Unit] =
    lessonRepositoryAlgebra
      .delete(classId)
      .value
      .void

  def updateLesson(lesson: Lesson)(implicit
      M: Monad[F]
  ): EitherT[F, LessonNotFoundError.type, Lesson] =
    for {
      saved <-
        lessonRepositoryAlgebra
          .update(lesson)
          .toRight(LessonNotFoundError)
    } yield saved

  def listLessons(
      pageSize: Int,
      offset: Int
  ): F[List[Lesson]] =
    lessonRepositoryAlgebra.list(pageSize, offset)

  def getLessonsByCourseId(courseId: Long): F[List[Lesson]] =
    lessonRepositoryAlgebra.getByCourseId(courseId)
}

object LessonService {
  def apply[F[_]](
      lessonRepositoryAlgebra: LessonRepositoryAlgebra[F]
  ): LessonService[F] =
    new LessonService(
      lessonRepositoryAlgebra
    )
}
