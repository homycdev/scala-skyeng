package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.payment

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.payment.{
  Transaction,
  TransactionRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class TransactionRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends TransactionRepositoryAlgebra[F]
    with IdentityStore[F, Long, Transaction] {
  private val cache = new TrieMap[Long, Transaction]

  private val random = new Random

  def create(transaction: Transaction): F[Transaction] = {
    val id = random.nextLong()
    val toSave = transaction.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(transaction: Transaction): OptionT[F, Transaction] =
    OptionT {
      transaction.id.traverse { id =>
        cache.update(id, transaction)
        transaction.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Transaction] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Transaction] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Transaction]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByStudentId(studentId: Long): F[List[Transaction]] =
    cache.values.filter(u => u.studentId == studentId).toList.pure[F]
}

object TransactionRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new TransactionRepositoryInMemoryInterpreter[F]
}
