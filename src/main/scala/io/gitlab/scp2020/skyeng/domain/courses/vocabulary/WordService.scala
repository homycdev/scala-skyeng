package io.gitlab.scp2020.skyeng.domain.courses.vocabulary

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  WordAlreadyExistsError,
  WordNotFoundError
}

class WordService[F[_]](
    wordRepositoryAlgebra: WordRepositoryAlgebra[F]
) {
  def createWord(word: Word)(implicit
      M: Monad[F]
  ): EitherT[F, WordAlreadyExistsError, Word] =
    for {
      saved <- EitherT.liftF(wordRepositoryAlgebra.create(word))
    } yield saved

  def getWord(wordId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, WordNotFoundError.type, Word] =
    wordRepositoryAlgebra
      .get(wordId)
      .toRight(WordNotFoundError)

  def deleteWord(wordId: Long)(implicit F: Functor[F]): F[Unit] =
    wordRepositoryAlgebra
      .delete(wordId)
      .value
      .void

  def updateWord(word: Word)(implicit
      M: Monad[F]
  ): EitherT[F, WordNotFoundError.type, Word] =
    for {
      saved <-
        wordRepositoryAlgebra
          .update(word)
          .toRight(WordNotFoundError)
    } yield saved

  def listWords(
      pageSize: Int,
      offset: Int
  ): F[List[Word]] =
    wordRepositoryAlgebra.list(pageSize, offset)
}

object WordService {
  def apply[F[_]](
      wordRepositoryAlgebra: WordRepositoryAlgebra[F]
  ): WordService[F] =
    new WordService(
      wordRepositoryAlgebra
    )
}
