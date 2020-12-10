package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.classes

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.classes.{Homework, HomeworkRepositoryAlgebra}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class HomeworkRepositoryInMemoryInterpreter[F[_]: Applicative] extends HomeworkRepositoryAlgebra[F]
  with IdentityStore[F, Long, Homework]
{
  private val cache = new TrieMap[Long, Homework]

  private val random = new Random

  def create(homework: Homework): F[Homework] = {
    val id = random.nextLong()
    val toSave = homework.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(homework: Homework): OptionT[F, Homework] =
    OptionT {
      homework.id.traverse { id =>
        cache.update(id, homework)
        homework.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Homework] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Homework] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Homework]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByCourseId(courseId: Long): F[List[Homework]] =
    cache.values.filter(u => u.courseId.contains(courseId)).toList.pure[F]

}

object HomeworkRepositoryInMemoryInterpreter{
  def apply[F[_] : Applicative](): HomeworkRepositoryInMemoryInterpreter[F] = new HomeworkRepositoryInMemoryInterpreter[F]
}
