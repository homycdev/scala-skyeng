package io.gitlab.scp2020.skyeng.domain.courses.exercises

import cats.data.OptionT

trait ExerciseRepositoryAlgebra[F[_]] {
  def create(exercise: Exercise): F[Exercise]

  def update(exercise: Exercise): OptionT[F, Exercise]

  def get(exerciseId: Long): OptionT[F, Exercise]

  def delete(exerciseId: Long): OptionT[F, Exercise]

  def list(pageSize: Int, offset: Int): F[List[Exercise]]

  def getByTaskId(taskId: Long): F[List[Exercise]]
}
