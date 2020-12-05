package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.users

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie.implicits.{toSqlInterpolator, _}
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import io.gitlab.scp2020.skyeng.domain.users.student.{
  StudentProfile,
  StudentProfileRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object StudentProfileSQL {
  def insert(profile: StudentProfile): Update0 =
    sql"""
    INSERT INTO student_profile(user_id, teacher_id, balance)
    VALUES (${profile.userId}, ${profile.teacherId}, ${profile.balance})
  """.stripMargin.update

  def update(profile: StudentProfile, id: Long): Update0 =
    sql"""
    UPDATE student_profile
    SET user_id = ${profile.userId}, teacher_id = ${profile.teacherId}, balance = ${profile.balance}
    WHERE user_id = $id
  """.stripMargin.update

  def delete(studentId: Long): Update0 =
    sql"""
    DELETE FROM student_profile WHERE user_id = $studentId
  """.stripMargin.update

  def select(studentId: Long): Query0[StudentProfile] =
    sql"""
    SELECT user_id, teacher_id, balance
    FROM student_profile
    WHERE user_id = $studentId
  """.stripMargin.query[StudentProfile]

  def selectAll: Query0[StudentProfile] =
    sql"""
    SELECT user_id, teacher_id, balance
    FROM student_profile
  """.query[StudentProfile]

  def selectByTeacherId(teacherId: Long): Query0[StudentProfile] =
    sql"""
    SELECT user_id, teacher_id, balance
    FROM student_profile
    WHERE teacher_id = $teacherId
  """.query[StudentProfile]
}

class DoobieStudentProfileRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends StudentProfileRepositoryAlgebra[F]
    with IdentityStore[F, Long, StudentProfile] {
  self =>

  import StudentProfileSQL._

  override def create(student: StudentProfile): F[StudentProfile] =
    insert(student).run
      .transact(xa)
      .as(student)

  override def update(student: StudentProfile): OptionT[F, StudentProfile] =
    OptionT
      .fromOption[F](Some(student.userId))
      .semiflatMap(id =>
        StudentProfileSQL
          .update(student, id)
          .run
          .transact(xa)
          .as(student)
      )

  override def get(id: Long): OptionT[F, StudentProfile] =
    OptionT(select(id).option.transact(xa))

  override def delete(studentId: Long): OptionT[F, StudentProfile] =
    get(studentId)
      .semiflatMap(student =>
        StudentProfileSQL
          .delete(studentId)
          .run
          .transact(xa)
          .as(student)
      )

  def list(pageSize: Int, offset: Int): F[List[StudentProfile]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByTeacherId(teacherId: Long): F[List[StudentProfile]] =
    selectByTeacherId(teacherId).to[List].transact(xa)
}

object DoobieStudentProfileRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieStudentProfileRepositoryInterpreter[F] =
    new DoobieStudentProfileRepositoryInterpreter(xa)
}
