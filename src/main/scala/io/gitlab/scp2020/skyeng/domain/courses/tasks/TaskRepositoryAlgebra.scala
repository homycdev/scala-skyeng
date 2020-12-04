package io.gitlab.scp2020.skyeng.domain.courses.tasks

import cats.data.OptionT

trait TaskRepositoryAlgebra[F[_]] {
  def create(task: Task): F[Task]

  def update(task: Task): OptionT[F, Task]

  def get(taskId: Long): OptionT[F, Task]

  def delete(taskId: Long): OptionT[F, Task]

  def list(pageSize: Int, offset: Int): F[List[Task]]

  def getByClassId(classId: Long): F[List[Task]]
}
