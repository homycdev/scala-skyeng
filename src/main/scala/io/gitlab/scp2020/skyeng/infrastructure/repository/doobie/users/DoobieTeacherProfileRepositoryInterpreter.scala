package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.users

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import io.gitlab.scp2020.skyeng.domain.users.teacher.{
  TeacherProfile,
  TeacherProfileRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object TeacherProfileSQL {

  def insert(profile: TeacherProfile): Update0 =
    sql"""
    INSERT INTO teacher_profile(user_id, bio, greeting, qualification)
    VALUES (${profile.userId}, ${profile.bio}, ${profile.greeting}, ${profile.qualification})
  """.stripMargin.update

  def update(profile: TeacherProfile, id: Long): Update0 =
    sql"""
    UPDATE teacher_profile
    SET bio = ${profile.bio},
    greeting = ${profile.greeting}, qualification = ${profile.qualification}
    WHERE user_id = $id
  """.update

  def select(teacherId: Long): Query0[TeacherProfile] =
    sql"""
    SELECT user_id, bio, greeting, qualification
    FROM teacher_profile
    WHERE user_id = $teacherId
  """.query[TeacherProfile]

  def delete(teacherId: Long): Update0 =
    sql"""
    DELETE FROM teacher_profile WHERE user_id = $teacherId
  """.update

  def selectAll: Query0[TeacherProfile] =
    sql"""
    SELECT user_id, bio, greeting, qualification
    FROM teacher_profile
  """.query[TeacherProfile]
}

class DoobieTeacherProfileRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends TeacherProfileRepositoryAlgebra[F]
    with IdentityStore[F, Long, TeacherProfile] {
  self =>

  import TeacherProfileSQL._

  def create(profile: TeacherProfile): F[TeacherProfile] =
    insert(profile).run
      .transact(xa)
      .as(profile)

  def update(profile: TeacherProfile): OptionT[F, TeacherProfile] =
    OptionT.fromOption[F](Some(profile.userId)).semiflatMap { id =>
      TeacherProfileSQL.update(profile, id).run.transact(xa).as(profile)
    }

  def get(teacherId: Long): OptionT[F, TeacherProfile] =
    OptionT(select(teacherId).option.transact(xa))

  def delete(teacherId: Long): OptionT[F, TeacherProfile] =
    get(teacherId).semiflatMap(teacher =>
      TeacherProfileSQL.delete(teacherId).run.transact(xa).as(teacher)
    )

  def list(pageSize: Int, offset: Int): F[List[TeacherProfile]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object DoobieTeacherProfileRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieTeacherProfileRepositoryInterpreter[F] =
    new DoobieTeacherProfileRepositoryInterpreter(xa)
}
