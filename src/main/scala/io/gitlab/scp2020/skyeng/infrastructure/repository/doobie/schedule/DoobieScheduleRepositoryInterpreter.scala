package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.schedule

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.schedule.{
  Schedule,
  ScheduleRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object ScheduleSQL {
  def insert(schedule: Schedule): Update0 =
    sql"""
    INSERT INTO schedule (student_id, teacher_id, start_time, duration_sec)
    VALUES (${schedule.studentId}, ${schedule.teacherId}, 
    ${schedule.startTime}, ${schedule.durationSeconds})
  """.update

  def update(schedule: Schedule, id: Long): Update0 =
    sql"""
    UPDATE schedule
    SET student_id = ${schedule.studentId}, teacher_id = ${schedule.teacherId},
    start_time = ${schedule.startTime}, duration_sec = ${schedule.durationSeconds} 
    WHERE id = $id
  """.update

  def select(scheduleId: Long): Query0[Schedule] =
    sql"""
    SELECT id, student_id, teacher_id, start_time, duration_sec
    FROM schedule
    WHERE id = $scheduleId
  """.query[Schedule]

  def delete(scheduleId: Long): Update0 =
    sql"""
    DELETE FROM schedule WHERE id = $scheduleId
  """.update

  def selectAll: Query0[Schedule] =
    sql"""
    SELECT id, student_id, teacher_id, start_time, duration_sec
    FROM schedule
  """.query[Schedule]

  def selectByStudentId(studentId: Long): Query0[Schedule] =
    sql"""
    SELECT id, student_id, teacher_id, start_time, duration_sec
    FROM schedule
    WHERE student_id = $studentId
  """.query[Schedule]

  def selectByTeacherId(teacherId: Long): Query0[Schedule] =
    sql"""
    SELECT id, student_id, teacher_id, start_time, duration_sec
    FROM schedule
    WHERE teacher_id = $teacherId
  """.query[Schedule]
}

class DoobieScheduleRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends ScheduleRepositoryAlgebra[F]
    with IdentityStore[F, Long, Schedule] {
  self =>

  import ScheduleSQL._

  def create(schedule: Schedule): F[Schedule] =
    insert(schedule)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => schedule.copy(id = id.some))
      .transact(xa)

  def update(schedule: Schedule): OptionT[F, Schedule] =
    OptionT.fromOption[F](schedule.id).semiflatMap { id =>
      ScheduleSQL.update(schedule, id).run.transact(xa).as(schedule)
    }

  def get(scheduleId: Long): OptionT[F, Schedule] =
    OptionT(select(scheduleId).option.transact(xa))

  def delete(scheduleId: Long): OptionT[F, Schedule] =
    get(scheduleId).semiflatMap(schedule =>
      ScheduleSQL.delete(scheduleId).run.transact(xa).as(schedule)
    )

  def list(pageSize: Int, offset: Int): F[List[Schedule]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByStudentId(studentId: Long): F[List[Schedule]] =
    selectByStudentId(studentId).to[List].transact(xa)

  def getByTeacherId(teacherId: Long): F[List[Schedule]] =
    selectByTeacherId(teacherId).to[List].transact(xa)
}

object DoobieScheduleRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieScheduleRepositoryInterpreter[F] =
    new DoobieScheduleRepositoryInterpreter(xa)
}
