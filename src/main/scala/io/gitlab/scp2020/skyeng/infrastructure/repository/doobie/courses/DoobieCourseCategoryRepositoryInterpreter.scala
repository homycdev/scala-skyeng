package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie.implicits.{toSqlInterpolator, _}
import doobie.{Query0, Transactor, Update0}
import io.gitlab.scp2020.skyeng.domain.courses.{
  CourseCategory,
  CourseCategoryRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object CourseCategorySQL {
  def insert(category: CourseCategory): Update0 =
    sql"""
    INSERT INTO course (title)
    VALUES (${category.title})
  """.update

  def update(category: CourseCategory, id: Long): Update0 =
    sql"""
    UPDATE course
    SET title = ${category.title}
    WHERE id = $id
  """.update

  def select(categoryId: Long): Query0[CourseCategory] =
    sql"""
    SELECT id, title
    FROM course
    WHERE id = $categoryId
  """.query[CourseCategory]

  def delete(categoryId: Long): Update0 =
    sql"""
    DELETE FROM course WHERE id = $categoryId
  """.update

  def selectAll: Query0[CourseCategory] =
    sql"""
    SELECT id, title
    FROM course
  """.query[CourseCategory]
}

class DoobieCourseCategoryRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends CourseCategoryRepositoryAlgebra[F]
    with IdentityStore[F, Long, CourseCategory] {
  self =>

  import CourseCategorySQL._

  def create(category: CourseCategory): F[CourseCategory] =
    insert(category)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => category.copy(id = id.some))
      .transact(xa)

  def update(category: CourseCategory): OptionT[F, CourseCategory] =
    OptionT.fromOption[F](category.id).semiflatMap { id =>
      CourseCategorySQL.update(category, id).run.transact(xa).as(category)
    }

  def get(categoryId: Long): OptionT[F, CourseCategory] =
    OptionT(select(categoryId).option.transact(xa))

  def delete(categoryId: Long): OptionT[F, CourseCategory] =
    get(categoryId).semiflatMap(category =>
      CourseCategorySQL.delete(categoryId).run.transact(xa).as(category)
    )

  def list(pageSize: Int, offset: Int): F[List[CourseCategory]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object DoobieCourseCategoryRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieCourseCategoryRepositoryInterpreter[F] =
    new DoobieCourseCategoryRepositoryInterpreter(xa)
}
