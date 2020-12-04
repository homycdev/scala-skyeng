package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.exercises

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.courses.exercises.{
  Exercise,
  ExerciseRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object ExerciseSQL {
  def insert(exercise: Exercise): Update0 =
    sql"""
    INSERT INTO exercise (task_id, type, content)
    VALUES (${exercise.taskId}, ${exercise.exerciseType}, ${exercise.content})
  """.update

  def update(exercise: Exercise, id: Long): Update0 =
    sql"""
    UPDATE exercise
    SET task_id = ${exercise.taskId}, type = ${exercise.exerciseType}, content = ${exercise.content}
    WHERE id = $id
  """.update

  def select(exerciseId: Long): Query0[Exercise] =
    sql"""
    SELECT id, task_id, type, content
    FROM exercise
    WHERE id = $exerciseId
  """.query[Exercise]

  def delete(exerciseId: Long): Update0 =
    sql"""
    DELETE FROM exercise WHERE id = $exerciseId
  """.update

  def selectAll: Query0[Exercise] =
    sql"""
    SELECT id, task_id, type, content
    FROM exercise
  """.query[Exercise]

  def selectByTaskId(taskId: Long): Query0[Exercise] =
    sql"""
    SELECT id, task_id, type, content
    FROM exercise
    WHERE task_id = $taskId
  """.query[Exercise]
}

class DoobieExerciseRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends ExerciseRepositoryAlgebra[F]
    with IdentityStore[F, Long, Exercise] {
  self =>

  import ExerciseSQL._

  def create(exercise: Exercise): F[Exercise] =
    insert(exercise)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => exercise.copy(id = id.some))
      .transact(xa)

  def update(exercise: Exercise): OptionT[F, Exercise] =
    OptionT.fromOption[F](exercise.id).semiflatMap { id =>
      ExerciseSQL.update(exercise, id).run.transact(xa).as(exercise)
    }

  def get(exerciseId: Long): OptionT[F, Exercise] =
    OptionT(select(exerciseId).option.transact(xa))

  def delete(exerciseId: Long): OptionT[F, Exercise] =
    get(exerciseId).semiflatMap(exercise =>
      ExerciseSQL.delete(exerciseId).run.transact(xa).as(exercise)
    )

  def list(pageSize: Int, offset: Int): F[List[Exercise]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByTaskId(taskId: Long): F[List[Exercise]] =
    selectByTaskId(taskId).to[List].transact(xa)
}

object DoobieExerciseRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieExerciseRepositoryInterpreter[F] =
    new DoobieExerciseRepositoryInterpreter(xa)
}
