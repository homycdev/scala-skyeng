package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.tasks

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.courses.tasks.{
  Task,
  TaskRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object TaskSQL {
  def insert(task: Task): Update0 =
    sql"""
    INSERT INTO task (class_id, type, list_position)
    VALUES (${task.classId}, ${task.taskType}, ${task.listPosition})
  """.update

  def update(task: Task, id: Long): Update0 =
    sql"""
    UPDATE task
    SET class_id = ${task.classId}, type = ${task.taskType}, list_position = ${task.listPosition}
    WHERE id = $id
  """.update

  def select(taskId: Long): Query0[Task] =
    sql"""
    SELECT id, class_id, type, list_position
    FROM task
    WHERE id = $taskId
    ORDER BY list_position
  """.query[Task]

  def delete(taskId: Long): Update0 =
    sql"""
    DELETE FROM task WHERE id = $taskId
  """.update

  def selectAll: Query0[Task] =
    sql"""
    SELECT id, class_id, type, list_position
    FROM task
    ORDER BY list_position
  """.query[Task]

  def selectByClassId(classId: Long): Query0[Task] =
    sql"""
    SELECT id, class_id, type, list_position
    FROM task
    WHERE class_id = $classId
    ORDER BY list_position
  """.query[Task]
}

class DoobieTaskRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends TaskRepositoryAlgebra[F]
    with IdentityStore[F, Long, Task] {
  self =>

  import TaskSQL._

  def create(task: Task): F[Task] =
    insert(task)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => task.copy(id = id.some))
      .transact(xa)

  def update(task: Task): OptionT[F, Task] =
    OptionT.fromOption[F](task.id).semiflatMap { id =>
      TaskSQL.update(task, id).run.transact(xa).as(task)
    }

  def get(taskId: Long): OptionT[F, Task] =
    OptionT(select(taskId).option.transact(xa))

  def delete(taskId: Long): OptionT[F, Task] =
    get(taskId).semiflatMap(task =>
      TaskSQL.delete(taskId).run.transact(xa).as(task)
    )

  def list(pageSize: Int, offset: Int): F[List[Task]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByClassId(classId: Long): F[List[Task]] =
    selectByClassId(classId).to[List].transact(xa)
}

object DoobieTaskRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieTaskRepositoryInterpreter[F] =
    new DoobieTaskRepositoryInterpreter(xa)
}
