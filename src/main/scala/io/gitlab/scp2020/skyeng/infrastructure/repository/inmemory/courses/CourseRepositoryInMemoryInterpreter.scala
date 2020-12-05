package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.{Course, CourseRepositoryAlgebra}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class CourseRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends CourseRepositoryAlgebra[F]
    with IdentityStore[F, Long, Course] {
  private val cache = new TrieMap[Long, Course]

  private val random = new Random

  def create(course: Course): F[Course] = {
    val id = random.nextLong()
    val toSave = course.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(course: Course): OptionT[F, Course] =
    OptionT {
      course.id.traverse { id =>
        cache.update(id, course)
        course.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Course] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Course] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Course]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByCategoryId(categoryId: Long): F[List[Course]] =
    cache.values.filter(u => u.categoryId.contains(categoryId)).toList.pure[F]
}

object CourseRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new CourseRepositoryInMemoryInterpreter[F]
}
