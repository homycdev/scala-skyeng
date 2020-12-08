package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.payment

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.payment.{
  Transaction,
  TransactionRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object TransactionSQL {
  def insert(transaction: Transaction): Update0 =
    sql"""
    INSERT INTO transaction (student_id, teacher_id, created, status, change, reminder)
    VALUES (${transaction.studentId}, ${transaction.teacherId}, ${transaction.created},
    ${transaction.status}, ${transaction.change}, ${transaction.reminder})
  """.update

  def update(transaction: Transaction, id: Long): Update0 =
    sql"""
    UPDATE transaction
    SET student_id = ${transaction.studentId}, teacher_id = ${transaction.teacherId},
    created = ${transaction.created}, status = ${transaction.status},
    change = ${transaction.change}, reminder = ${transaction.reminder} 
    WHERE id = $id
  """.update

  def select(transactionId: Long): Query0[Transaction] =
    sql"""
    SELECT id, student_id, teacher_id, created, status, change, reminder
    FROM transaction
    WHERE id = $transactionId
  """.query[Transaction]

  def delete(transactionId: Long): Update0 =
    sql"""
    DELETE FROM transaction WHERE id = $transactionId
  """.update

  def selectAll: Query0[Transaction] =
    sql"""
    SELECT id, student_id, teacher_id, created, status, change, reminder
    FROM transaction
    ORDER BY created
  """.query[Transaction]

  def selectByStudentId(studentId: Long): Query0[Transaction] =
    sql"""
    SELECT id, student_id, teacher_id, created, status, change, reminder
    FROM transaction
    WHERE student_id = $studentId
    ORDER BY created
  """.query[Transaction]
}

class DoobieTransactionRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends TransactionRepositoryAlgebra[F]
    with IdentityStore[F, Long, Transaction] {
  self =>

  import TransactionSQL._

  def create(transaction: Transaction): F[Transaction] =
    insert(transaction)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => transaction.copy(id = id.some))
      .transact(xa)

  def update(transaction: Transaction): OptionT[F, Transaction] =
    OptionT.fromOption[F](transaction.id).semiflatMap { id =>
      TransactionSQL.update(transaction, id).run.transact(xa).as(transaction)
    }

  def get(transactionId: Long): OptionT[F, Transaction] =
    OptionT(select(transactionId).option.transact(xa))

  def delete(transactionId: Long): OptionT[F, Transaction] =
    get(transactionId).semiflatMap(transaction =>
      TransactionSQL.delete(transactionId).run.transact(xa).as(transaction)
    )

  def list(pageSize: Int, offset: Int): F[List[Transaction]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByStudentId(studentId: Long): F[List[Transaction]] =
    selectByStudentId(studentId).to[List].transact(xa)
}

object DoobieTransactionRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieTransactionRepositoryInterpreter[F] =
    new DoobieTransactionRepositoryInterpreter(xa)
}
