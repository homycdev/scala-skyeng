package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.classes

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  ClassVocabulary,
  ClassVocabularyRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class ClassVocabularyRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends ClassVocabularyRepositoryAlgebra[F]
    with IdentityStore[F, Long, ClassVocabulary] {
  private val cache = new TrieMap[Long, ClassVocabulary]

  private val random = new Random

  def create(vocabulary: ClassVocabulary): F[ClassVocabulary] = {
    val id = random.nextLong()
    val toSave = vocabulary.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(vocabulary: ClassVocabulary): OptionT[F, ClassVocabulary] =
    OptionT {
      vocabulary.id.traverse { id =>
        cache.update(id, vocabulary)
        vocabulary.pure[F]
      }
    }

  def get(id: Long): OptionT[F, ClassVocabulary] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, ClassVocabulary] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[ClassVocabulary]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByClassId(classId: Long): F[List[ClassVocabulary]] =
    cache.values.filter(u => u.classId == classId).toList.pure[F]
}

object ClassVocabularyRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new ClassVocabularyRepositoryInMemoryInterpreter[F]
}
