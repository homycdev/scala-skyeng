package io.gitlab.scp2020.skyeng.domain.results

import cats.data.OptionT

trait ExerciseResultRepositoryAlgebra[F[_]] {
  def create(result: ExerciseResult): F[ExerciseResult]

  def update(result: ExerciseResult): OptionT[F, ExerciseResult]

  def get(resultId: Long): OptionT[F, ExerciseResult]

  def delete(resultId: Long): OptionT[F, ExerciseResult]

  def list(pageSize: Int, offset: Int): F[List[ExerciseResult]]

  def getByStudentId(studentId: Long): F[List[ExerciseResult]]

  def getByExerciseId(exerciseId: Long): F[List[ExerciseResult]]
}
