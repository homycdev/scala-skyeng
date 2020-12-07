package io.gitlab.scp2020.skyeng.domain.courses.classes

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  ClassVocabularyNotFoundError,
  StudentAlreadyExistsError
}

class ClassVocabularyService[F[_]](
    vocabularyRepositoryAlgebra: ClassVocabularyRepositoryAlgebra[F]
) {
  def createClassVocabulary(vocabulary: ClassVocabulary)(implicit
      M: Monad[F]
  ): EitherT[F, StudentAlreadyExistsError, ClassVocabulary] =
    for {
      saved <- EitherT.liftF(vocabularyRepositoryAlgebra.create(vocabulary))
    } yield saved

  def getClassVocabulary(vocabularyId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, ClassVocabularyNotFoundError.type, ClassVocabulary] =
    vocabularyRepositoryAlgebra
      .get(vocabularyId)
      .toRight(ClassVocabularyNotFoundError)

  def deleteClassVocabulary(
      vocabularyId: Long
  )(implicit F: Functor[F]): F[Unit] =
    vocabularyRepositoryAlgebra
      .delete(vocabularyId)
      .value
      .void

  def updateClassVocabulary(vocabulary: ClassVocabulary)(implicit
      M: Monad[F]
  ): EitherT[F, ClassVocabularyNotFoundError.type, ClassVocabulary] =
    for {
      saved <-
        vocabularyRepositoryAlgebra
          .update(vocabulary)
          .toRight(ClassVocabularyNotFoundError)
    } yield saved

  def listClassVocabularies(
      pageSize: Int,
      offset: Int
  ): F[List[ClassVocabulary]] =
    vocabularyRepositoryAlgebra.list(pageSize, offset)

  def getClassVocabulariesByClassId(classId: Long): F[List[ClassVocabulary]] =
    vocabularyRepositoryAlgebra.getByClassId(classId)
}

object ClassVocabularyService {
  def apply[F[_]](
      vocabularyRepositoryAlgebra: ClassVocabularyRepositoryAlgebra[F]
  ): ClassVocabularyService[F] =
    new ClassVocabularyService(
      vocabularyRepositoryAlgebra
    )
}
