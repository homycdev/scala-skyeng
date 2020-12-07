package io.gitlab.scp2020.skyeng.domain.results

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  TaskResultAlreadyExistsError,
  TaskResultNotFoundError
}

class TaskResultService[F[_]](
    resultRepositoryAlgebra: TaskResultRepositoryAlgebra[F]
) {
  def createTaskResult(result: TaskResult)(implicit
      M: Monad[F]
  ): EitherT[F, TaskResultAlreadyExistsError, TaskResult] =
    for {
      saved <- EitherT.liftF(resultRepositoryAlgebra.create(result))
    } yield saved

  def getTaskResult(resultId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, TaskResultNotFoundError.type, TaskResult] =
    resultRepositoryAlgebra
      .get(resultId)
      .toRight(TaskResultNotFoundError)

  def deleteTaskResult(resultId: Long)(implicit F: Functor[F]): F[Unit] =
    resultRepositoryAlgebra
      .delete(resultId)
      .value
      .void

  def updateTaskResult(result: TaskResult)(implicit
      M: Monad[F]
  ): EitherT[F, TaskResultNotFoundError.type, TaskResult] =
    for {
      saved <-
        resultRepositoryAlgebra
          .update(result)
          .toRight(TaskResultNotFoundError)
    } yield saved

  def listTaskResults(pageSize: Int, offset: Int): F[List[TaskResult]] =
    resultRepositoryAlgebra.list(pageSize, offset)

  def getTaskResultsByStudentId(studentId: Long): F[List[TaskResult]] =
    resultRepositoryAlgebra.getByStudentId(studentId)

  def getTaskResultsByTaskId(taskId: Long): F[List[TaskResult]] =
    resultRepositoryAlgebra.getByTaskId(taskId)
}
