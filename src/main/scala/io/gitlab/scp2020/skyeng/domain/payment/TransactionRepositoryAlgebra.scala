package io.gitlab.scp2020.skyeng.domain.payment

import cats.data.OptionT

trait TransactionRepositoryAlgebra[F[_]] {
  def create(transaction: Transaction): F[Transaction]

  def update(transaction: Transaction): OptionT[F, Transaction]

  def get(transactionId: Long): OptionT[F, Transaction]

  def delete(transactionId: Long): OptionT[F, Transaction]

  def list(pageSize: Int, offset: Int): F[List[Transaction]]

  def getByStudentId(studentId: Long): F[List[Transaction]]
}
