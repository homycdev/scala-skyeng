package io.gitlab.scp2020.skyeng.domain.courses

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  CourseAlreadyExistsError,
  CourseNotFoundError
}

class CourseService[F[_]](
    courseRepositoryAlgebra: CourseRepositoryAlgebra[F]
) {
  def createCourse(course: Course)(implicit
      M: Monad[F]
  ): EitherT[F, CourseAlreadyExistsError, Course] =
    for {
      saved <- EitherT.liftF(courseRepositoryAlgebra.create(course))
    } yield saved

  def getCourse(courseId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, CourseNotFoundError.type, Course] =
    courseRepositoryAlgebra
      .get(courseId)
      .toRight(CourseNotFoundError)

  def deleteCourse(courseId: Long)(implicit F: Functor[F]): F[Unit] =
    courseRepositoryAlgebra
      .delete(courseId)
      .value
      .void

  def updateCourse(course: Course)(implicit
      M: Monad[F]
  ): EitherT[F, CourseNotFoundError.type, Course] =
    for {
      saved <-
        courseRepositoryAlgebra
          .update(course)
          .toRight(CourseNotFoundError)
    } yield saved

  def listCourses(
      pageSize: Int,
      offset: Int
  ): F[List[Course]] =
    courseRepositoryAlgebra.list(pageSize, offset)

  def getCoursesByCategoryId(categoryId: Long): F[List[Course]] =
    courseRepositoryAlgebra.getByCategoryId(categoryId)
}

object CourseService {
  def apply[F[_]](
      courseRepositoryAlgebra: CourseRepositoryAlgebra[F]
  ): CourseService[F] =
    new CourseService(
      courseRepositoryAlgebra
    )
}
