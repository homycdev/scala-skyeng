package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.{
  CourseCategory,
  CourseCategoryRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class CourseCategoryRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends CourseCategoryRepositoryAlgebra[F]
    with IdentityStore[F, Long, CourseCategory] {
  private val cache = new TrieMap[Long, CourseCategory]

  private val random = new Random

  def create(category: CourseCategory): F[CourseCategory] = {
    val id = random.nextLong()
    val toSave = category.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(category: CourseCategory): OptionT[F, CourseCategory] =
    OptionT {
      category.id.traverse { id =>
        cache.update(id, category)
        category.pure[F]
      }
    }

  def get(id: Long): OptionT[F, CourseCategory] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, CourseCategory] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[CourseCategory]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]
}

object CourseCategoryRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new CourseCategoryRepositoryInMemoryInterpreter[F]
}
