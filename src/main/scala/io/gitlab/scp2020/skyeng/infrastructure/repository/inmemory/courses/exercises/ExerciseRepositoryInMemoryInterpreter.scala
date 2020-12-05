package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.exercises

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.exercises.{
  Exercise,
  ExerciseRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class ExerciseRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends ExerciseRepositoryAlgebra[F]
    with IdentityStore[F, Long, Exercise] {
  private val cache = new TrieMap[Long, Exercise]

  private val random = new Random

  def create(exercise: Exercise): F[Exercise] = {
    val id = random.nextLong()
    val toSave = exercise.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(exercise: Exercise): OptionT[F, Exercise] =
    OptionT {
      exercise.id.traverse { id =>
        cache.update(id, exercise)
        exercise.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Exercise] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Exercise] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Exercise]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByTaskId(taskId: Long): F[List[Exercise]] =
    cache.values.filter(u => u.taskId.contains(taskId)).toList.pure[F]
}

object ExerciseRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new ExerciseRepositoryInMemoryInterpreter[F]
}
