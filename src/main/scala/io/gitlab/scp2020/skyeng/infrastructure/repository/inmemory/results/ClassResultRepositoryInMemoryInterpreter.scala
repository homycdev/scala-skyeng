package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.results

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.results.{
  ClassResult,
  ClassResultRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class ClassResultRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends ClassResultRepositoryAlgebra[F]
    with IdentityStore[F, Long, ClassResult] {
  private val cache = new TrieMap[Long, ClassResult]

  private val random = new Random

  def create(result: ClassResult): F[ClassResult] = {
    val id = random.nextLong()
    val toSave = result.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(result: ClassResult): OptionT[F, ClassResult] =
    OptionT {
      result.id.traverse { id =>
        cache.update(id, result)
        result.pure[F]
      }
    }

  def get(id: Long): OptionT[F, ClassResult] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, ClassResult] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[ClassResult]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByStudentId(studentId: Long): F[List[ClassResult]] =
    cache.values.filter(u => u.studentId == studentId).toList.pure[F]

  def getByClassId(classId: Long): F[List[ClassResult]] =
    cache.values.filter(u => u.classId == classId).toList.pure[F]
}

object ClassResultRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new ClassResultRepositoryInMemoryInterpreter[F]
}
