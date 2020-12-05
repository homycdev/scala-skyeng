package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.schedule

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.schedule.{Room, RoomRepositoryAlgebra}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object RoomSQL {
  def insert(room: Room): Update0 =
    sql"""
    INSERT INTO room (student_id, teacher_id, url, is_open)
    VALUES (${room.studentId}, ${room.teacherId}, ${room.url}, ${room.isOpen})
  """.update

  def update(room: Room, id: Long): Update0 =
    sql"""
    UPDATE room
    SET student_id = ${room.studentId}, teacher_id = ${room.teacherId},
    url = ${room.url}, is_open = ${room.isOpen} 
    WHERE id = $id
  """.update

  def select(roomId: Long): Query0[Room] =
    sql"""
    SELECT id, student_id, teacher_id, url, is_open
    FROM room
    WHERE id = $roomId
  """.query[Room]

  def delete(roomId: Long): Update0 =
    sql"""
    DELETE FROM room WHERE id = $roomId
  """.update

  def selectAll: Query0[Room] =
    sql"""
    SELECT id, student_id, teacher_id, url, is_open
    FROM room
  """.query[Room]

  def selectByStudentId(studentId: Long): Query0[Room] =
    sql"""
    SELECT id, student_id, teacher_id, url, is_open
    FROM room
    WHERE student_id = $studentId
  """.query[Room]

  def selectByTeacherId(teacherId: Long): Query0[Room] =
    sql"""
    SELECT id, student_id, teacher_id, url, is_open
    FROM room
    WHERE teacher_id = $teacherId
  """.query[Room]
}

class DoobieRoomRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends RoomRepositoryAlgebra[F]
    with IdentityStore[F, Long, Room] {
  self =>

  import RoomSQL._

  def create(room: Room): F[Room] =
    insert(room)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => room.copy(id = id.some))
      .transact(xa)

  def update(room: Room): OptionT[F, Room] =
    OptionT.fromOption[F](room.id).semiflatMap { id =>
      RoomSQL.update(room, id).run.transact(xa).as(room)
    }

  def get(roomId: Long): OptionT[F, Room] =
    OptionT(select(roomId).option.transact(xa))

  def delete(roomId: Long): OptionT[F, Room] =
    get(roomId).semiflatMap(room =>
      RoomSQL.delete(roomId).run.transact(xa).as(room)
    )

  def list(pageSize: Int, offset: Int): F[List[Room]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByStudentId(studentId: Long): F[List[Room]] =
    selectByStudentId(studentId).to[List].transact(xa)

  def getByTeacherId(teacherId: Long): F[List[Room]] =
    selectByTeacherId(teacherId).to[List].transact(xa)
}

object DoobieRoomRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieRoomRepositoryInterpreter[F] =
    new DoobieRoomRepositoryInterpreter(xa)
}
