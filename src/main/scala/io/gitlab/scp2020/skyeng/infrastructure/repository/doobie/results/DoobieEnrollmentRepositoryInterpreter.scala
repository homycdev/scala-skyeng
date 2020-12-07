package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.results

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.courses.{
  Enrollment,
  EnrollmentRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object EnrollmentSQL {
  def insert(enrollment: Enrollment): Update0 =
    sql"""
    INSERT INTO enrollment (student_id, course_id)
    VALUES (${enrollment.studentId}, ${enrollment.courseId})
  """.update

  def update(enrollment: Enrollment, id: Long): Update0 =
    sql"""
    UPDATE enrollment
    SET student_id = ${enrollment.studentId}, course_id = ${enrollment.courseId} 
    WHERE id = $id
  """.update

  def select(enrollmentId: Long): Query0[Enrollment] =
    sql"""
    SELECT id, student_id, course_id
    FROM enrollment
    WHERE id = $enrollmentId
  """.query[Enrollment]

  def delete(enrollmentId: Long): Update0 =
    sql"""
    DELETE FROM enrollment WHERE id = $enrollmentId
  """.update

  def selectAll: Query0[Enrollment] =
    sql"""
    SELECT id, student_id, course_id
    FROM enrollment
  """.query[Enrollment]

  def selectByStudentId(studentId: Long): Query0[Enrollment] =
    sql"""
    SELECT id, student_id, course_id
    FROM enrollment
    WHERE student_id = $studentId
  """.query[Enrollment]

  def selectByCourseId(courseId: Long): Query0[Enrollment] =
    sql"""
    SELECT id, student_id, course_id
    FROM enrollment
    WHERE course_id = $courseId
  """.query[Enrollment]
}

class DoobieEnrollmentRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends EnrollmentRepositoryAlgebra[F]
    with IdentityStore[F, Long, Enrollment] {
  self =>

  import EnrollmentSQL._

  def create(enrollment: Enrollment): F[Enrollment] =
    insert(enrollment)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => enrollment.copy(id = id.some))
      .transact(xa)

  def update(enrollment: Enrollment): OptionT[F, Enrollment] =
    OptionT.fromOption[F](enrollment.id).semiflatMap { id =>
      EnrollmentSQL.update(enrollment, id).run.transact(xa).as(enrollment)
    }

  def get(enrollmentId: Long): OptionT[F, Enrollment] =
    OptionT(select(enrollmentId).option.transact(xa))

  def delete(enrollmentId: Long): OptionT[F, Enrollment] =
    get(enrollmentId).semiflatMap(enrollment =>
      EnrollmentSQL.delete(enrollmentId).run.transact(xa).as(enrollment)
    )

  def list(pageSize: Int, offset: Int): F[List[Enrollment]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByStudentId(studentId: Long): F[List[Enrollment]] =
    selectByStudentId(studentId).to[List].transact(xa)

  def getByCourseId(courseId: Long): F[List[Enrollment]] =
    selectByCourseId(courseId).to[List].transact(xa)
}

object DoobieEnrollmentRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieEnrollmentRepositoryInterpreter[F] =
    new DoobieEnrollmentRepositoryInterpreter(xa)
}
