package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.vocabulary

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.vocabulary.{
  Word,
  WordRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class WordRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends WordRepositoryAlgebra[F]
    with IdentityStore[F, Long, Word] {
  private val cache = new TrieMap[Long, Word]

  private val random = new Random

  def create(word: Word): F[Word] = {
    val id = random.nextLong()
    val toSave = word.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(word: Word): OptionT[F, Word] =
    OptionT {
      word.id.traverse { id =>
        cache.update(id, word)
        word.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Word] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Word] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Word]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]
}

object WordRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new WordRepositoryInMemoryInterpreter[F]
}
