package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import io.gitlab.scp2020.skyeng.domain.users.teacher.{QualificationType, TeacherProfile, TeacherRepositoryAlgebra}
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.{fromQualificationType, toQualificationType}
import tsec.authentication.IdentityStore

private object TeacherSQL {
  implicit val qualificationTypeGet: Get[QualificationType] = Get[String].tmap(fromQualificationType)
  implicit val qualificationTypePut: Put[QualificationType] = Put[String].tcontramap(toQualificationType)


  def createTeacherFromUser(newTeacher: TeacherProfile): Update0 =
    sql"""
    INSERT INTO teacher_profile(user_id, bio, greeting, qualification)
         |VALUES (${newTeacher.userId}, ${newTeacher.bio}, ${newTeacher.greeting}, ${newTeacher.qualification})
         |""".stripMargin.update

  def updateTeacherId(teacher: TeacherProfile, id: Long): Update0 =
    sql"""
         UPDATE teacher_profile
         set user_id = ${teacher.userId}, bio = ${teacher.bio},
         greeting = ${teacher.greeting}, qualification = ${teacher.qualification}
         where user_id = $id
       """.update

  def deleteTeacherProfile(teacherId: Long): Update0 =
    sql"""
         delete from teacher_profile where user_id = $teacherId
       """.update

  def getTeacherProfile(teacherId: Long): Query0[TeacherProfile] =
    sql"""
         select user_id, bio, greeting, qualification
         from teacher_profile
         where user_id = $teacherId
       """.query[TeacherProfile]

}


class DoobieTeacherInterpreter[F[_] : Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends TeacherRepositoryAlgebra[F]
    with IdentityStore[F, Long, TeacherProfile] {
  self =>

  import TeacherSQL._

  def create(newTeacher: TeacherProfile): F[TeacherProfile] =
    createTeacherFromUser(newTeacher)
      .run
      .transact(xa)
      .as(newTeacher)

  // TODO implement these queries
  override def update(teacher: TeacherProfile): OptionT[F, TeacherProfile] =
    OptionT.fromOption[F](Some(teacher.userId)).semiflatMap{
      id => TeacherSQL.updateTeacherId(teacher, id).run.transact(xa).as(teacher)
    }

  override def get(id: Long): OptionT[F, TeacherProfile] =
    OptionT(getTeacherProfile(id).option.transact(xa))

  override def delete(teacherId: Long): OptionT[F, TeacherProfile] =
    get(teacherId).semiflatMap(teacher => TeacherSQL.deleteTeacherProfile(teacherId).run.transact(xa).as(teacher))

//  override def list(pageSize: Int, offset: Int): F[List[TeacherProfile]] = ???
}
