package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.classes

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  Lesson,
  LessonRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class LessonRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends LessonRepositoryAlgebra[F]
    with IdentityStore[F, Long, Lesson] {
  private val cache = new TrieMap[Long, Lesson]

  private val random = new Random

  override def create(lesson: Lesson): F[Lesson] = {
    val id = random.nextLong()
    val toSave = lesson.copy(id = lesson.id)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(lesson: Lesson): OptionT[F, Lesson] =
    OptionT {
      lesson.id.traverse { id =>
        cache.update(id, lesson)
        lesson.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Lesson] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Lesson] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Lesson]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByCourseId(courseId: Long): F[List[Lesson]] =
    cache.values.filter(u => u.courseId.contains(courseId)).toList.pure[F]
}
object LessonRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]: LessonRepositoryInMemoryInterpreter[F] =
    new LessonRepositoryInMemoryInterpreter[F]
}
