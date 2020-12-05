package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.results

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.results.{
  TaskResult,
  TaskResultRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object TaskResultSQL {
  def insert(result: TaskResult): Update0 =
    sql"""
    INSERT INTO task_result (student_id, task_id, score)
    VALUES (${result.studentId}, ${result.taskId}, ${result.score})
  """.update

  def update(result: TaskResult, id: Long): Update0 =
    sql"""
    UPDATE task_result
    SET student_id = ${result.studentId}, task_id = ${result.taskId},
    score = ${result.score} 
    WHERE id = $id
  """.update

  def select(resultId: Long): Query0[TaskResult] =
    sql"""
    SELECT id, student_id, task_id, score
    FROM task_result
    WHERE id = $resultId
  """.query[TaskResult]

  def delete(resultId: Long): Update0 =
    sql"""
    DELETE FROM task_result WHERE id = $resultId
  """.update

  def selectAll: Query0[TaskResult] =
    sql"""
    SELECT id, student_id, task_id, score
    FROM task_result
  """.query[TaskResult]

  def selectByStudentId(studentId: Long): Query0[TaskResult] =
    sql"""
    SELECT id, student_id, task_id, score
    FROM task_result
    WHERE student_id = $studentId
  """.query[TaskResult]

  def selectByTaskId(taskId: Long): Query0[TaskResult] =
    sql"""
    SELECT id, student_id, task_id, score
    FROM task_result
    WHERE task_id = $taskId
  """.query[TaskResult]
}

class DoobieTaskResultRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends TaskResultRepositoryAlgebra[F]
    with IdentityStore[F, Long, TaskResult] {
  self =>

  import TaskResultSQL._

  def create(result: TaskResult): F[TaskResult] =
    insert(result)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => result.copy(id = id.some))
      .transact(xa)

  def update(result: TaskResult): OptionT[F, TaskResult] =
    OptionT.fromOption[F](result.id).semiflatMap { id =>
      TaskResultSQL.update(result, id).run.transact(xa).as(result)
    }

  def get(resultId: Long): OptionT[F, TaskResult] =
    OptionT(select(resultId).option.transact(xa))

  def delete(resultId: Long): OptionT[F, TaskResult] =
    get(resultId).semiflatMap(result =>
      TaskResultSQL.delete(resultId).run.transact(xa).as(result)
    )

  def list(pageSize: Int, offset: Int): F[List[TaskResult]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByStudentId(studentId: Long): F[List[TaskResult]] =
    selectByStudentId(studentId).to[List].transact(xa)

  def getByTaskId(taskId: Long): F[List[TaskResult]] =
    selectByTaskId(taskId).to[List].transact(xa)
}

object DoobieTaskResultRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieTaskResultRepositoryInterpreter[F] =
    new DoobieTaskResultRepositoryInterpreter(xa)
}
