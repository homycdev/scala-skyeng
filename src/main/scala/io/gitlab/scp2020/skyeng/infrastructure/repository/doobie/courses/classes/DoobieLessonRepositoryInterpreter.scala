package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.classes

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  ClassType,
  Lesson,
  LessonRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object LessonSQL {
  def insert(
      homework: Lesson,
      classType: ClassType = ClassType.Lesson
  ): Update0 =
    sql"""
    INSERT INTO class (title, course_id, type, difficulty, list_position)
    VALUES (${homework.title}, ${homework.courseId}, $classType,  
    ${homework.difficulty}, ${homework.listPosition})
  """.update

  def update(
      lesson: Lesson,
      id: Long,
      classType: ClassType = ClassType.Lesson
  ): Update0 =
    sql"""
    UPDATE class
    SET title = ${lesson.title}, course_id = ${lesson.courseId}, 
    difficulty = ${lesson.difficulty}, list_position = ${lesson.listPosition}
    WHERE id = $id AND type = $classType
  """.update

  def select(
      lessonId: Long,
      classType: ClassType = ClassType.Lesson
  ): Query0[Lesson] =
    sql"""
    SELECT id, title, course_id, difficulty, list_position
    FROM class
    WHERE id = $lessonId AND type = $classType
    ORDER BY list_position
  """.query[Lesson]

  def delete(lessonId: Long, classType: ClassType = ClassType.Lesson): Update0 =
    sql"""
    DELETE FROM class WHERE id = $lessonId AND type = $classType
  """.update

  def selectAll(classType: ClassType = ClassType.Lesson): Query0[Lesson] =
    sql"""
    SELECT id, title, course_id, difficulty, list_position
    FROM class
    WHERE type = $classType
    ORDER BY list_position
  """.query[Lesson]

  def selectByCourseId(
      courseId: Long,
      classType: ClassType = ClassType.Lesson
  ): Query0[Lesson] =
    sql"""
    SELECT id, title, course_id, difficulty, list_position
    FROM class
    WHERE course_id = $courseId AND type = $classType
    ORDER BY list_position
  """.query[Lesson]
}

class DoobieLessonRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends LessonRepositoryAlgebra[F]
    with IdentityStore[F, Long, Lesson] {
  self =>

  import LessonSQL._

  def create(lesson: Lesson): F[Lesson] =
    insert(lesson)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => lesson.copy(id = id.some))
      .transact(xa)

  def update(lesson: Lesson): OptionT[F, Lesson] =
    OptionT.fromOption[F](lesson.id).semiflatMap { id =>
      LessonSQL.update(lesson, id).run.transact(xa).as(lesson)
    }

  def get(lessonId: Long): OptionT[F, Lesson] =
    OptionT(select(lessonId).option.transact(xa))

  def delete(lessonId: Long): OptionT[F, Lesson] =
    get(lessonId).semiflatMap(lesson =>
      LessonSQL.delete(lessonId).run.transact(xa).as(lesson)
    )

  def list(pageSize: Int, offset: Int): F[List[Lesson]] =
    paginate(pageSize, offset)(selectAll()).to[List].transact(xa)

  def getByCourseId(courseId: Long): F[List[Lesson]] =
    selectByCourseId(courseId).to[List].transact(xa)
}

object DoobieLessonRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieLessonRepositoryInterpreter[F] = {
    new DoobieLessonRepositoryInterpreter(xa)
  }
}
