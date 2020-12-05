package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.schedule

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.schedule.{
  Schedule,
  ScheduleRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class ScheduleRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends ScheduleRepositoryAlgebra[F]
    with IdentityStore[F, Long, Schedule] {
  private val cache = new TrieMap[Long, Schedule]

  private val random = new Random

  def create(schedule: Schedule): F[Schedule] = {
    val id = random.nextLong()
    val toSave = schedule.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(schedule: Schedule): OptionT[F, Schedule] =
    OptionT {
      schedule.id.traverse { id =>
        cache.update(id, schedule)
        schedule.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Schedule] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Schedule] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Schedule]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByStudentId(studentId: Long): F[List[Schedule]] =
    cache.values.filter(u => u.studentId == studentId).toList.pure[F]

  def getByTeacherId(teacherId: Long): F[List[Schedule]] =
    cache.values.filter(u => u.teacherId == teacherId).toList.pure[F]
}

object ScheduleRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new ScheduleRepositoryInMemoryInterpreter[F]
}
