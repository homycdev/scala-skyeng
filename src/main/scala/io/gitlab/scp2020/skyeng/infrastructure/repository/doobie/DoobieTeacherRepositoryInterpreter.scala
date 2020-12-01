package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie

//import doobie.postgres.syntax._
//import doobie.postgres.implicits._

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.users.teacher.{QualificationType, TeacherProfile, TeacherRepositoryAlgebra}
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.{fromQualificationType, toQualificationType}
import tsec.authentication.IdentityStore

private object TeacherSQL {
  implicit val qualificationTypeGet: Get[Option[QualificationType]] = Get[String].tmap(fromQualificationType)
  implicit val qualificationTypePut: Put[QualificationType] = Put[String].tcontramap(toQualificationType)


  def createTeacherFromUser(user: User, teacher: TeacherProfile): Update0 =
    sql"""
    INSERT INTO teacher_profile(user_id, bio, greeting, qualification)
         |VALUES (${user.id}, ${teacher.bio}, ${teacher.greeting}, ${teacher.qualification})
         |""".stripMargin.update
}


class DoobieTeacherInterpreter[F[_] : Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends TeacherRepositoryAlgebra[F]
    with IdentityStore[F, Long, TeacherProfile] {
  self =>
  def create(potentialTeacher: User, newTeacher: TeacherProfile): F[TeacherProfile] =
    TeacherSQL
      .createTeacherFromUser(potentialTeacher, newTeacher)
      .run
      .transact(xa)
      .as(newTeacher)

  // TODO implement these queries
  override def update(teacher: TeacherProfile): F[TeacherProfile] = ???

  override def delete(teacherId: Long): OptionT[F, TeacherProfile] = ???

  override def list(pageSize: Int, offset: Int): F[List[TeacherProfile]] = ???

  override def get(id: Long): OptionT[F, TeacherProfile] = ???
}
