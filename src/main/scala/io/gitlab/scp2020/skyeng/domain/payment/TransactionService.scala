package io.gitlab.scp2020.skyeng.domain.payment

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  TransactionAlreadyExistsError,
  TransactionNotFoundError
}

class TransactionService[F[_]](
    transactionRepositoryAlgebra: TransactionRepositoryAlgebra[F]
) {
  def createTransaction(transaction: Transaction)(implicit
      M: Monad[F]
  ): EitherT[F, TransactionAlreadyExistsError, Transaction] =
    for {
      saved <- EitherT.liftF(transactionRepositoryAlgebra.create(transaction))
    } yield saved

  def getTransaction(transactionId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, TransactionNotFoundError.type, Transaction] =
    transactionRepositoryAlgebra
      .get(transactionId)
      .toRight(TransactionNotFoundError)

  def deleteTransaction(transactionId: Long)(implicit F: Functor[F]): F[Unit] =
    transactionRepositoryAlgebra
      .delete(transactionId)
      .value
      .void

  def updateTransaction(transaction: Transaction)(implicit
      M: Monad[F]
  ): EitherT[F, TransactionNotFoundError.type, Transaction] =
    for {
      saved <-
        transactionRepositoryAlgebra
          .update(transaction)
          .toRight(TransactionNotFoundError)
    } yield saved

  def listTransactions(
      pageSize: Int,
      offset: Int
  ): F[List[Transaction]] =
    transactionRepositoryAlgebra.list(pageSize, offset)

  def getTasksByStudentId(studentId: Long): F[List[Transaction]] =
    transactionRepositoryAlgebra.getByStudentId(studentId)
}

object TransactionService {
  def apply[F[_]](
      transactionRepositoryAlgebra: TransactionRepositoryAlgebra[F]
  ): TransactionService[F] =
    new TransactionService(
      transactionRepositoryAlgebra
    )
}
