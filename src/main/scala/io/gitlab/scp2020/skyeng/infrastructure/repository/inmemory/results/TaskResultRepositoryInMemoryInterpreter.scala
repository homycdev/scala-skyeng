package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.results

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.results.{
  TaskResult,
  TaskResultRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class TaskResultRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends TaskResultRepositoryAlgebra[F]
    with IdentityStore[F, Long, TaskResult] {
  private val cache = new TrieMap[Long, TaskResult]

  private val random = new Random

  def create(result: TaskResult): F[TaskResult] = {
    val id = random.nextLong()
    val toSave = result.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(result: TaskResult): OptionT[F, TaskResult] =
    OptionT {
      result.id.traverse { id =>
        cache.update(id, result)
        result.pure[F]
      }
    }

  def get(id: Long): OptionT[F, TaskResult] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, TaskResult] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[TaskResult]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByStudentId(studentId: Long): F[List[TaskResult]] =
    cache.values.filter(u => u.studentId == studentId).toList.pure[F]

  def getByTaskId(taskId: Long): F[List[TaskResult]] =
    cache.values.filter(u => u.taskId == taskId).toList.pure[F]
}

object TaskResultRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new TaskResultRepositoryInMemoryInterpreter[F]
}
