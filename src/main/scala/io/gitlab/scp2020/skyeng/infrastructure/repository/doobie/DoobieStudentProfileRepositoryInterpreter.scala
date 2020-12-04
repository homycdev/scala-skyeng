package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie.implicits.{toSqlInterpolator, _}
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import doobie.{Get, Put}
import io.gitlab.scp2020.skyeng.domain.users.student.{StudentProfile, StudentRepositoryAlgebra}
import io.gitlab.scp2020.skyeng.domain.users.teacher.{QualificationType, TeacherProfile}
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.{fromQualificationType, toQualificationType}
import tsec.authentication.IdentityStore

private object StudentSQL {
  implicit val qualificationTypeGet: Get[QualificationType] = Get[String].tmap(fromQualificationType)
  implicit val qualificationTypePut: Put[QualificationType] = Put[String].tcontramap(toQualificationType)

  def createStudent(newStudent: StudentProfile): Update0 =
    sql"""
         |insert into student_profile(user_id, teacher_id, balance)
         |values (${newStudent.userId}, ${newStudent.teacherId}, ${newStudent.balance})
         |""".stripMargin.update

  def updateStudentProfile(student: StudentProfile, id: Long): Update0 =
    sql"""
         |update student_profile
         |set user_id = ${student.userId}, teacher_id = ${student.teacherId}, balance = ${student.balance}
         |where user_id = $id
         |""".stripMargin.update

  def deleteStudentProfile(studentId: Long): Update0 =
    sql"""
         |delete from student_profile where user_id = $studentId
         |""".stripMargin.update


  def getStudentProfile(studentId: Long): Query0[StudentProfile] =
    sql"""
         |select user_id, teacher_id, balance
         |from student_profile
         |where user_id = $studentId
         |""".stripMargin.query[StudentProfile]

  def getTeacherOfStudent(studentId: Long): Query0[TeacherProfile] =
    sql"""
         |select * from teacher_profile left join student_profile sp on teacher_profile.user_id = sp.user_id
         |where sp.user_id = $studentId
         |""".stripMargin.query[TeacherProfile]

  def getStudentProfileBalance(studentId: Long): Query0[Int] =
    sql"""
         |select balance from student_profile
         |where user_id = $studentId
         |""".stripMargin.query[Int]

}


class DoobieStudentProfileRepository[F[_] : Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends StudentRepositoryAlgebra[F]
    with IdentityStore[F, Long, StudentProfile] {
  self =>

  import StudentSQL._

  override def create(student: StudentProfile): F[StudentProfile] =
    createStudent(student)
      .run
      .transact(xa)
      .as(student)

  override def update(student: StudentProfile): OptionT[F, StudentProfile] =
    OptionT.fromOption[F](Some(student.userId))
      .semiflatMap(id => StudentSQL.updateStudentProfile(student, id).run.transact(xa).as(student))

  override def get(id: Long): OptionT[F, StudentProfile] =
    OptionT(getStudentProfile(id).option.transact(xa))

  override def delete(studentId: Long): OptionT[F, StudentProfile] =
    get(studentId)
      .semiflatMap(student => StudentSQL
        .deleteStudentProfile(studentId)
        .run
        .transact(xa).as(student))

  override def getTeacher(studentId: Long): OptionT[F, TeacherProfile] =
    OptionT(getTeacherOfStudent(studentId).option.transact(xa))

  override def getBalance(studentId: Long): OptionT[F, Int] =
    OptionT(getStudentProfileBalance(studentId).option.transact(xa))
}
