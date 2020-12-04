package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie.implicits.{toSqlInterpolator, _}
import doobie.{Query0, Transactor, Update0}
import io.gitlab.scp2020.skyeng.domain.courses.{Course, CourseRepositoryAlgebra}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object CourseSQL {
  def insert(course: Course): Update0 =
    sql"""
    INSERT INTO course (title, category_id)
    VALUES (${course.title}, ${course.categoryId})
  """.update

  def update(course: Course, id: Long): Update0 =
    sql"""
    UPDATE course
    SET title = ${course.title}, category_id = ${course.categoryId}
    WHERE id = $id
  """.update

  def select(courseId: Long): Query0[Course] =
    sql"""
    SELECT id, title, category_id
    FROM course
    WHERE id = $courseId
  """.query[Course]

  def delete(courseId: Long): Update0 =
    sql"""
    DELETE FROM course WHERE id = $courseId
  """.update

  def selectAll: Query0[Course] =
    sql"""
    SELECT id, title, category_id
    FROM course
  """.query[Course]

  def selectByCategoryId(categoryId: Long): Query0[Course] =
    sql"""
    SELECT id, title, category_id
    FROM course
    WHERE category_id = $categoryId
  """.query[Course]
}

class DoobieCourseRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends CourseRepositoryAlgebra[F]
    with IdentityStore[F, Long, Course] {
  self =>

  import CourseSQL._

  def create(course: Course): F[Course] =
    insert(course)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => course.copy(id = id.some))
      .transact(xa)

  def update(course: Course): OptionT[F, Course] =
    OptionT.fromOption[F](course.id).semiflatMap { id =>
      CourseSQL.update(course, id).run.transact(xa).as(course)
    }

  def get(courseId: Long): OptionT[F, Course] =
    OptionT(select(courseId).option.transact(xa))

  def delete(courseId: Long): OptionT[F, Course] =
    get(courseId).semiflatMap(course =>
      CourseSQL.delete(courseId).run.transact(xa).as(course)
    )

  def list(pageSize: Int, offset: Int): F[List[Course]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByCategoryId(categoryId: Long): F[List[Course]] =
    selectByCategoryId(categoryId).to[List].transact(xa)
}

object DoobieCourseRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieCourseRepositoryInterpreter[F] =
    new DoobieCourseRepositoryInterpreter(xa)
}
