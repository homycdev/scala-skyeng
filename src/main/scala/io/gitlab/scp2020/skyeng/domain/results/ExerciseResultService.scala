package io.gitlab.scp2020.skyeng.domain.results

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  ExerciseResultAlreadyExistsError,
  ExerciseResultNotFoundError
}

class ExerciseResultService[F[_]](
    resultRepositoryAlgebra: ExerciseResultRepositoryAlgebra[F]
) {
  def createExerciseResult(result: ExerciseResult)(implicit
      M: Monad[F]
  ): EitherT[F, ExerciseResultAlreadyExistsError, ExerciseResult] =
    for {
      saved <- EitherT.liftF(resultRepositoryAlgebra.create(result))
    } yield saved

  def getExerciseResult(resultId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, ExerciseResultNotFoundError.type, ExerciseResult] =
    resultRepositoryAlgebra
      .get(resultId)
      .toRight(ExerciseResultNotFoundError)

  def deleteExerciseResult(resultId: Long)(implicit F: Functor[F]): F[Unit] =
    resultRepositoryAlgebra
      .delete(resultId)
      .value
      .void

  def updateExerciseResult(result: ExerciseResult)(implicit
      M: Monad[F]
  ): EitherT[F, ExerciseResultNotFoundError.type, ExerciseResult] =
    for {
      saved <-
        resultRepositoryAlgebra
          .update(result)
          .toRight(ExerciseResultNotFoundError)
    } yield saved

  def listExerciseResults(pageSize: Int, offset: Int): F[List[ExerciseResult]] =
    resultRepositoryAlgebra.list(pageSize, offset)

  def getExerciseResultsByStudentId(studentId: Long): F[List[ExerciseResult]] =
    resultRepositoryAlgebra.getByStudentId(studentId)

  def getExerciseResultsByExerciseId(
      exerciseId: Long
  ): F[List[ExerciseResult]] =
    resultRepositoryAlgebra.getByExerciseId(exerciseId)
}
