package io.gitlab.scp2020.skyeng.domain.courses.classes

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  HomeworkAlreadyExistsError,
  HomeworkNotFoundError
}

class HomeworkService[F[_]](
    homeworkRepositoryAlgebra: HomeworkRepositoryAlgebra[F]
) {
  def createHomework(homework: Homework)(implicit
      M: Monad[F]
  ): EitherT[F, HomeworkAlreadyExistsError, Homework] =
    for {
      saved <- EitherT.liftF(homeworkRepositoryAlgebra.create(homework))
    } yield saved

  def getHomework(classId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, HomeworkNotFoundError.type, Homework] =
    homeworkRepositoryAlgebra
      .get(classId)
      .toRight(HomeworkNotFoundError)

  def deleteHomework(
      classId: Long
  )(implicit F: Functor[F]): F[Unit] =
    homeworkRepositoryAlgebra
      .delete(classId)
      .value
      .void

  def updateHomework(homework: Homework)(implicit
      M: Monad[F]
  ): EitherT[F, HomeworkNotFoundError.type, Homework] =
    for {
      saved <-
        homeworkRepositoryAlgebra
          .update(homework)
          .toRight(HomeworkNotFoundError)
    } yield saved

  def listHomeworks(
      pageSize: Int,
      offset: Int
  ): F[List[Homework]] =
    homeworkRepositoryAlgebra.list(pageSize, offset)

  def getHomeworksByCourseId(courseId: Long): F[List[Homework]] =
    homeworkRepositoryAlgebra.getByCourseId(courseId)
}

object HomeworkService {
  def apply[F[_]](
      homeworkRepositoryAlgebra: HomeworkRepositoryAlgebra[F]
  ): HomeworkService[F] =
    new HomeworkService(
      homeworkRepositoryAlgebra
    )
}
