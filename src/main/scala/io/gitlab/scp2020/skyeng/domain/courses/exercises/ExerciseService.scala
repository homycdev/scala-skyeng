package io.gitlab.scp2020.skyeng.domain.courses.exercises

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  ExerciseAlreadyExistsError,
  ExerciseNotFoundError
}

class ExerciseService[F[_]](
    exerciseRepositoryAlgebra: ExerciseRepositoryAlgebra[F]
) {
  def createExercise(exercise: Exercise)(implicit
      M: Monad[F]
  ): EitherT[F, ExerciseAlreadyExistsError, Exercise] =
    for {
      saved <- EitherT.liftF(exerciseRepositoryAlgebra.create(exercise))
    } yield saved

  def getExercise(exerciseId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, ExerciseNotFoundError.type, Exercise] =
    exerciseRepositoryAlgebra
      .get(exerciseId)
      .toRight(ExerciseNotFoundError)

  def deleteExercise(exerciseId: Long)(implicit F: Functor[F]): F[Unit] =
    exerciseRepositoryAlgebra
      .delete(exerciseId)
      .value
      .void

  def updateExercise(exercise: Exercise)(implicit
      M: Monad[F]
  ): EitherT[F, ExerciseNotFoundError.type, Exercise] =
    for {
      saved <-
        exerciseRepositoryAlgebra
          .update(exercise)
          .toRight(ExerciseNotFoundError)
    } yield saved

  def listExercises(
      pageSize: Int,
      offset: Int
  ): F[List[Exercise]] =
    exerciseRepositoryAlgebra.list(pageSize, offset)

  def getExercisesByTaskId(taskId: Long): F[List[Exercise]] =
    exerciseRepositoryAlgebra.getByTaskId(taskId)
}

object ExerciseService {
  def apply[F[_]](
      exerciseRepositoryAlgebra: ExerciseRepositoryAlgebra[F]
  ): ExerciseService[F] =
    new ExerciseService(
      exerciseRepositoryAlgebra
    )
}
