package io.gitlab.scp2020.skyeng.domain.courses.classes

import cats.data.OptionT

trait ClassVocabularyRepositoryAlgebra[F[_]] {
  def create(vocabulary: ClassVocabulary): F[ClassVocabulary]

  def update(vocabulary: ClassVocabulary): OptionT[F, ClassVocabulary]

  def get(vocabularyId: Long): OptionT[F, ClassVocabulary]

  def delete(vocabularyId: Long): OptionT[F, ClassVocabulary]

  def list(pageSize: Int, offset: Int): F[List[ClassVocabulary]]

  def getByClassId(classId: Long): F[List[ClassVocabulary]]
}
