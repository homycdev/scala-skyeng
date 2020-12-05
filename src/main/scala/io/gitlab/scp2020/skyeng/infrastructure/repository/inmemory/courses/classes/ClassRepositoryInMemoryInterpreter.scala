package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.classes

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  Class,
  ClassRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class ClassRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends ClassRepositoryAlgebra[F]
    with IdentityStore[F, Long, Class] {
  private val cache = new TrieMap[Long, Class]

  private val random = new Random

  def create(classObj: Class): F[Class] = {
    val id = random.nextLong()
    val toSave = classObj.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(classObj: Class): OptionT[F, Class] =
    OptionT {
      classObj.id.traverse { id =>
        cache.update(id, classObj)
        classObj.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Class] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Class] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Class]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByCourseId(courseId: Long): F[List[Class]] =
    cache.values.filter(u => u.courseId.contains(courseId)).toList.pure[F]
}

object ClassRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new ClassRepositoryInMemoryInterpreter[F]
}
