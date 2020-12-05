package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.schedule

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.schedule.{Room, RoomRepositoryAlgebra}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class RoomRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends RoomRepositoryAlgebra[F]
    with IdentityStore[F, Long, Room] {
  private val cache = new TrieMap[Long, Room]

  private val random = new Random

  def create(room: Room): F[Room] = {
    val id = random.nextLong()
    val toSave = room.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(room: Room): OptionT[F, Room] =
    OptionT {
      room.id.traverse { id =>
        cache.update(id, room)
        room.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Room] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Room] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Room]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByStudentId(studentId: Long): F[List[Room]] =
    cache.values.filter(u => u.studentId == studentId).toList.pure[F]

  def getByTeacherId(teacherId: Long): F[List[Room]] =
    cache.values.filter(u => u.teacherId.contains(teacherId)).toList.pure[F]
}

object RoomRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new RoomRepositoryInMemoryInterpreter[F]
}
