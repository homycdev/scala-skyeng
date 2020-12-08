package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.results

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.results.{
  ExerciseResult,
  ExerciseResultRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import java.util.Random
import scala.collection.concurrent.TrieMap

class ExerciseResultRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends ExerciseResultRepositoryAlgebra[F]
    with IdentityStore[F, Long, ExerciseResult] {
  private val cache = new TrieMap[Long, ExerciseResult]

  private val random = new Random

  def create(result: ExerciseResult): F[ExerciseResult] = {
    val id = random.nextLong()
    val toSave = result.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(result: ExerciseResult): OptionT[F, ExerciseResult] =
    OptionT {
      result.id.traverse { id =>
        cache.update(id, result)
        result.pure[F]
      }
    }

  def get(id: Long): OptionT[F, ExerciseResult] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, ExerciseResult] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[ExerciseResult]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByStudentId(studentId: Long): F[List[ExerciseResult]] =
    cache.values.filter(u => u.studentId == studentId).toList.pure[F]

  def getByExerciseId(exerciseId: Long): F[List[ExerciseResult]] =
    cache.values.filter(u => u.exerciseId == exerciseId).toList.pure[F]

  def getByStudentIdAndExerciseId(
      studentId: Long,
      exerciseId: Long
  ): F[List[ExerciseResult]] =
    cache.values
      .filter(u => u.studentId == studentId && u.exerciseId == exerciseId)
      .toList
      .pure[F]
}

object ExerciseResultRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new ExerciseResultRepositoryInMemoryInterpreter[F]
}
