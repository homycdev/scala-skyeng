package io.gitlab.scp2020.skyeng.domain.courses.vocabulary

import cats.data.OptionT

trait WordRepositoryAlgebra[F[_]] {
  def create(word: Word): F[Word]

  def update(word: Word): OptionT[F, Word]

  def get(wordId: Long): OptionT[F, Word]

  def delete(wordId: Long): OptionT[F, Word]

  def list(pageSize: Int, offset: Int): F[List[Word]]
}
