package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.classes

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  ClassType,
  Homework,
  HomeworkRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object HomeworkSQL {
  def insert(
      homework: Homework,
      classType: ClassType = ClassType.Homework
  ): Update0 =
    sql"""
    INSERT INTO class (title, course_id, type, lesson_id, difficulty, list_position)
    VALUES (${homework.title}, ${homework.courseId}, $classType, ${homework.lessonId}, 
    ${homework.difficulty}, ${homework.listPosition})
  """.update

  def update(
      homework: Homework,
      id: Long,
      classType: ClassType = ClassType.Homework
  ): Update0 =
    sql"""
    UPDATE class
    SET title = ${homework.title}, course_id = ${homework.courseId}, 
    lesson_id = ${homework.lessonId}, difficulty = ${homework.difficulty}, 
    list_position = ${homework.listPosition}
    WHERE id = $id AND type = $classType
  """.update

  def select(
      homeworkId: Long,
      classType: ClassType = ClassType.Homework
  ): Query0[Homework] =
    sql"""
    SELECT id, title, course_id, lesson_id, difficulty, list_position
    FROM class
    WHERE id = $homeworkId AND type = $classType
    ORDER BY list_position
  """.query[Homework]

  def delete(
      homeworkId: Long,
      classType: ClassType = ClassType.Homework
  ): Update0 =
    sql"""
    DELETE FROM class WHERE id = $homeworkId AND type = $classType
  """.update

  def selectAll(classType: ClassType = ClassType.Homework): Query0[Homework] =
    sql"""
    SELECT id, title, course_id, lesson_id, difficulty, list_position
    FROM class
    WHERE type = $classType
    ORDER BY list_position
  """.query[Homework]

  def selectByCourseId(
      courseId: Long,
      classType: ClassType = ClassType.Homework
  ): Query0[Homework] =
    sql"""
    SELECT id, title, course_id, lesson_id, difficulty, list_position
    FROM class
    WHERE course_id = $courseId AND type = $classType
    ORDER BY list_position
  """.query[Homework]
}

class DoobieHomeworkRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends HomeworkRepositoryAlgebra[F]
    with IdentityStore[F, Long, Homework] {
  self =>

  import HomeworkSQL._

  def create(homework: Homework): F[Homework] =
    insert(homework)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => homework.copy(id = id.some))
      .transact(xa)

  def update(homework: Homework): OptionT[F, Homework] =
    OptionT.fromOption[F](homework.id).semiflatMap { id =>
      HomeworkSQL.update(homework, id).run.transact(xa).as(homework)
    }

  def get(homeworkId: Long): OptionT[F, Homework] =
    OptionT(select(homeworkId).option.transact(xa))

  def delete(homeworkId: Long): OptionT[F, Homework] =
    get(homeworkId).semiflatMap(homework =>
      HomeworkSQL.delete(homeworkId).run.transact(xa).as(homework)
    )

  def list(pageSize: Int, offset: Int): F[List[Homework]] =
    paginate(pageSize, offset)(selectAll()).to[List].transact(xa)

  def getByCourseId(courseId: Long): F[List[Homework]] =
    selectByCourseId(courseId).to[List].transact(xa)
}

object DoobieHomeworkRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieHomeworkRepositoryInterpreter[F] = {
    new DoobieHomeworkRepositoryInterpreter(xa)
  }
}
