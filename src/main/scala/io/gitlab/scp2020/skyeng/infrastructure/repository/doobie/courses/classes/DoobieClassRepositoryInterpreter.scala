package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.classes

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  Class,
  ClassRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object ClassSQL {
  def insert(classObj: Class): Update0 =
    sql"""
    INSERT INTO class (course_id, type, lesson_id, difficulty, list_position)
    VALUES (${classObj.courseId}, ${classObj.classType}, ${classObj.lessonId}, 
    ${classObj.difficulty}, ${classObj.listPosition})
  """.update

  def update(classObj: Class, id: Long): Update0 =
    sql"""
    UPDATE class
    SET course_id = ${classObj.courseId}, type = ${classObj.classType}, 
    lesson_id = ${classObj.lessonId}, difficulty = ${classObj.difficulty}, 
    list_position = ${classObj.listPosition}
    WHERE id = $id
  """.update

  def select(classId: Long): Query0[Class] =
    sql"""
    SELECT id, course_id, type, lesson_id, difficulty, list_position
    FROM class
    WHERE id = $classId
    ORDER BY list_position
  """.query[Class]

  def delete(classId: Long): Update0 =
    sql"""
    DELETE FROM class WHERE id = $classId
  """.update

  def selectAll: Query0[Class] =
    sql"""
    SELECT id, course_id, type, lesson_id, difficulty, list_position
    FROM class
    ORDER BY list_position
  """.query[Class]

  def selectByCourseId(courseId: Long): Query0[Class] =
    sql"""
    SELECT id, course_id, type, lesson_id, difficulty, list_position
    FROM class
    WHERE course_id = $courseId
    ORDER BY list_position
  """.query[Class]
}

class DoobieClassRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends ClassRepositoryAlgebra[F]
    with IdentityStore[F, Long, Class] {
  self =>

  import ClassSQL._

  def create(classObj: Class): F[Class] =
    insert(classObj)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => classObj.copy(id = id.some))
      .transact(xa)

  def update(classObj: Class): OptionT[F, Class] =
    OptionT.fromOption[F](classObj.id).semiflatMap { id =>
      ClassSQL.update(classObj, id).run.transact(xa).as(classObj)
    }

  def get(classId: Long): OptionT[F, Class] =
    OptionT(select(classId).option.transact(xa))

  def delete(classId: Long): OptionT[F, Class] =
    get(classId).semiflatMap(classObj =>
      ClassSQL.delete(classId).run.transact(xa).as(classObj)
    )

  def list(pageSize: Int, offset: Int): F[List[Class]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByCourseId(courseId: Long): F[List[Class]] =
    selectByCourseId(courseId).to[List].transact(xa)
}

object DoobieClassRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieClassRepositoryInterpreter[F] = {
    new DoobieClassRepositoryInterpreter(xa)
  }
}
