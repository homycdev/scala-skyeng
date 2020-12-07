package io.gitlab.scp2020.skyeng.domain.courses.tasks

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  TaskAlreadyExistsError,
  TaskNotFoundError
}

class TaskService[F[_]](
    taskRepositoryAlgebra: TaskRepositoryAlgebra[F]
) {
  def createTask(task: Task)(implicit
      M: Monad[F]
  ): EitherT[F, TaskAlreadyExistsError, Task] =
    for {
      saved <- EitherT.liftF(taskRepositoryAlgebra.create(task))
    } yield saved

  def getTask(taskId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, TaskNotFoundError.type, Task] =
    taskRepositoryAlgebra
      .get(taskId)
      .toRight(TaskNotFoundError)

  def deleteTask(taskId: Long)(implicit F: Functor[F]): F[Unit] =
    taskRepositoryAlgebra
      .delete(taskId)
      .value
      .void

  def updateTask(task: Task)(implicit
      M: Monad[F]
  ): EitherT[F, TaskNotFoundError.type, Task] =
    for {
      saved <-
        taskRepositoryAlgebra
          .update(task)
          .toRight(TaskNotFoundError)
    } yield saved

  def listTasks(
      pageSize: Int,
      offset: Int
  ): F[List[Task]] =
    taskRepositoryAlgebra.list(pageSize, offset)

  def getTasksByClassId(classId: Long): F[List[Task]] =
    taskRepositoryAlgebra.getByClassId(classId)
}

object TaskService {
  def apply[F[_]](
      taskRepositoryAlgebra: TaskRepositoryAlgebra[F]
  ): TaskService[F] =
    new TaskService(
      taskRepositoryAlgebra
    )
}
