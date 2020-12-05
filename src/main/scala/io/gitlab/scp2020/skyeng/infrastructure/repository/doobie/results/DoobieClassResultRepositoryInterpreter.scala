package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.results

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.results.{
  ClassResult,
  ClassResultRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object ClassResultSQL {
  def insert(result: ClassResult): Update0 =
    sql"""
    INSERT INTO class_result (student_id, class_id, score)
    VALUES (${result.studentId}, ${result.classId}, ${result.score})
  """.update

  def update(result: ClassResult, id: Long): Update0 =
    sql"""
    UPDATE class_result
    SET student_id = ${result.studentId}, class_id = ${result.classId},
    score = ${result.score} 
    WHERE id = $id
  """.update

  def select(resultId: Long): Query0[ClassResult] =
    sql"""
    SELECT id, student_id, class_id, score
    FROM class_result
    WHERE id = $resultId
  """.query[ClassResult]

  def delete(resultId: Long): Update0 =
    sql"""
    DELETE FROM class_result WHERE id = $resultId
  """.update

  def selectAll: Query0[ClassResult] =
    sql"""
    SELECT id, student_id, class_id, score
    FROM class_result
  """.query[ClassResult]

  def selectByStudentId(studentId: Long): Query0[ClassResult] =
    sql"""
    SELECT id, student_id, class_id, score
    FROM class_result
    WHERE student_id = $studentId
  """.query[ClassResult]

  def selectByClassId(classId: Long): Query0[ClassResult] =
    sql"""
    SELECT id, student_id, class_id, score
    FROM class_result
    WHERE class_id = $classId
  """.query[ClassResult]
}

class DoobieClassResultRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends ClassResultRepositoryAlgebra[F]
    with IdentityStore[F, Long, ClassResult] {
  self =>

  import ClassResultSQL._

  def create(result: ClassResult): F[ClassResult] =
    insert(result)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => result.copy(id = id.some))
      .transact(xa)

  def update(result: ClassResult): OptionT[F, ClassResult] =
    OptionT.fromOption[F](result.id).semiflatMap { id =>
      ClassResultSQL.update(result, id).run.transact(xa).as(result)
    }

  def get(resultId: Long): OptionT[F, ClassResult] =
    OptionT(select(resultId).option.transact(xa))

  def delete(resultId: Long): OptionT[F, ClassResult] =
    get(resultId).semiflatMap(result =>
      ClassResultSQL.delete(resultId).run.transact(xa).as(result)
    )

  def list(pageSize: Int, offset: Int): F[List[ClassResult]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByStudentId(studentId: Long): F[List[ClassResult]] =
    selectByStudentId(studentId).to[List].transact(xa)

  def getByClassId(classId: Long): F[List[ClassResult]] =
    selectByClassId(classId).to[List].transact(xa)
}

object DoobieClassResultRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieClassResultRepositoryInterpreter[F] =
    new DoobieClassResultRepositoryInterpreter(xa)
}
