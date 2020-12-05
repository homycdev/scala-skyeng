package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.results

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.results.{
  ExerciseResult,
  ExerciseResultRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object ExerciseResultSQL {
  def insert(result: ExerciseResult): Update0 =
    sql"""
    INSERT INTO exercise_result (student_id, exercise_id, score, content)
    VALUES (${result.studentId}, ${result.exerciseId}, ${result.score}, ${result.content})
  """.update

  def update(result: ExerciseResult, id: Long): Update0 =
    sql"""
    UPDATE exercise_result
    SET student_id = ${result.studentId}, exercise_id = ${result.exerciseId},
    score = ${result.score}, content = ${result.content} 
    WHERE id = $id
  """.update

  def select(resultId: Long): Query0[ExerciseResult] =
    sql"""
    SELECT id, student_id, exercise_id, score, content
    FROM exercise_result
    WHERE id = $resultId
  """.query[ExerciseResult]

  def delete(resultId: Long): Update0 =
    sql"""
    DELETE FROM exercise_result WHERE id = $resultId
  """.update

  def selectAll: Query0[ExerciseResult] =
    sql"""
    SELECT id, student_id, exercise_id, score, content 
    FROM exercise_result
  """.query[ExerciseResult]

  def selectByStudentId(studentId: Long): Query0[ExerciseResult] =
    sql"""
    SELECT id, student_id, exercise_id, score, content 
    FROM exercise_result
    WHERE student_id = $studentId
  """.query[ExerciseResult]

  def selectByExerciseId(exerciseId: Long): Query0[ExerciseResult] =
    sql"""
    SELECT id, student_id, exercise_id, score, content 
    FROM exercise_result
    WHERE exercise_id = $exerciseId
  """.query[ExerciseResult]
}

class DoobieExerciseResultRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends ExerciseResultRepositoryAlgebra[F]
    with IdentityStore[F, Long, ExerciseResult] {
  self =>

  import ExerciseResultSQL._

  def create(result: ExerciseResult): F[ExerciseResult] =
    insert(result)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => result.copy(id = id.some))
      .transact(xa)

  def update(result: ExerciseResult): OptionT[F, ExerciseResult] =
    OptionT.fromOption[F](result.id).semiflatMap { id =>
      ExerciseResultSQL.update(result, id).run.transact(xa).as(result)
    }

  def get(resultId: Long): OptionT[F, ExerciseResult] =
    OptionT(select(resultId).option.transact(xa))

  def delete(resultId: Long): OptionT[F, ExerciseResult] =
    get(resultId).semiflatMap(result =>
      ExerciseResultSQL.delete(resultId).run.transact(xa).as(result)
    )

  def list(pageSize: Int, offset: Int): F[List[ExerciseResult]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByStudentId(studentId: Long): F[List[ExerciseResult]] =
    selectByStudentId(studentId).to[List].transact(xa)

  def getByExerciseId(exerciseId: Long): F[List[ExerciseResult]] =
    selectByExerciseId(exerciseId).to[List].transact(xa)
}

object DoobieExerciseResultRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieExerciseResultRepositoryInterpreter[F] =
    new DoobieExerciseResultRepositoryInterpreter(xa)
}
