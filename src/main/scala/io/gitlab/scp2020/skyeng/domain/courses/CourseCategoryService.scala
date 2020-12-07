package io.gitlab.scp2020.skyeng.domain.courses

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  CourseCategoryAlreadyExistsError,
  CourseCategoryNotFoundError
}

class CourseCategoryService[F[_]](
    categoryRepositoryAlgebra: CourseCategoryRepositoryAlgebra[F]
) {
  def createCourseCategory(category: CourseCategory)(implicit
      M: Monad[F]
  ): EitherT[F, CourseCategoryAlreadyExistsError, CourseCategory] =
    for {
      saved <- EitherT.liftF(categoryRepositoryAlgebra.create(category))
    } yield saved

  def getCourseCategory(categoryId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, CourseCategoryNotFoundError.type, CourseCategory] =
    categoryRepositoryAlgebra
      .get(categoryId)
      .toRight(CourseCategoryNotFoundError)

  def deleteCourseCategory(categoryId: Long)(implicit F: Functor[F]): F[Unit] =
    categoryRepositoryAlgebra
      .delete(categoryId)
      .value
      .void

  def updateCourseCategory(category: CourseCategory)(implicit
      M: Monad[F]
  ): EitherT[F, CourseCategoryNotFoundError.type, CourseCategory] =
    for {
      saved <-
        categoryRepositoryAlgebra
          .update(category)
          .toRight(CourseCategoryNotFoundError)
    } yield saved

  def listCourseCategories(
      pageSize: Int,
      offset: Int
  ): F[List[CourseCategory]] =
    categoryRepositoryAlgebra.list(pageSize, offset)
}

object CourseCategoryService {
  def apply[F[_]](
      categoryRepositoryAlgebra: CourseCategoryRepositoryAlgebra[F]
  ): CourseCategoryService[F] =
    new CourseCategoryService(
      categoryRepositoryAlgebra
    )
}
