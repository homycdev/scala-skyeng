package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.tasks

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.tasks.{
  Task,
  TaskRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class TaskRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends TaskRepositoryAlgebra[F]
    with IdentityStore[F, Long, Task] {
  private val cache = new TrieMap[Long, Task]

  private val random = new Random

  def create(task: Task): F[Task] = {
    val id = random.nextLong()
    val toSave = task.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(task: Task): OptionT[F, Task] =
    OptionT {
      task.id.traverse { id =>
        cache.update(id, task)
        task.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Task] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Task] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Task]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByClassId(classId: Long): F[List[Task]] =
    cache.values.filter(u => u.classId.contains(classId)).toList.pure[F]
}

object TaskRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new TaskRepositoryInMemoryInterpreter[F]
}
