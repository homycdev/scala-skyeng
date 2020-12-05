package io.gitlab.scp2020.skyeng.domain.results

import cats.data.OptionT

trait TaskResultRepositoryAlgebra[F[_]] {
  def create(result: TaskResult): F[TaskResult]

  def update(result: TaskResult): OptionT[F, TaskResult]

  def get(resultId: Long): OptionT[F, TaskResult]

  def delete(resultId: Long): OptionT[F, TaskResult]

  def list(pageSize: Int, offset: Int): F[List[TaskResult]]

  def getByStudentId(studentId: Long): F[List[TaskResult]]

  def getByTaskId(taskId: Long): F[List[TaskResult]]
}
