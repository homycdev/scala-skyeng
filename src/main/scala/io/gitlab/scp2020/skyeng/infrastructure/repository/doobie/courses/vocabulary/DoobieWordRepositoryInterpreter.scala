package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.vocabulary

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie.implicits.{toSqlInterpolator, _}
import doobie.{Query0, Transactor, Update0}
import io.gitlab.scp2020.skyeng.domain.courses.vocabulary.{
  Word,
  WordRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object WordSQL {
  def insert(word: Word): Update0 =
    sql"""
    INSERT INTO word (phrase, image_url, transcript, english_meaning, russian_translation)
    VALUES (${word.phrase}, ${word.imageUrl}, ${word.transcript}, ${word.englishMeaning}, ${word.russianTranslation})
  """.update

  def update(word: Word, id: Long): Update0 =
    sql"""
    UPDATE word
    SET phrase = ${word.phrase}, image_url = ${word.imageUrl}, transcript = ${word.transcript}, 
    english_meaning = ${word.englishMeaning}, russian_translation = ${word.russianTranslation}
    WHERE id = $id
  """.update

  def select(wordId: Long): Query0[Word] =
    sql"""
    SELECT id, phrase, image_url, transcript, english_meaning, russian_translation
    FROM word
    WHERE id = $wordId
  """.query[Word]

  def delete(wordId: Long): Update0 =
    sql"""
    DELETE FROM word WHERE id = $wordId
  """.update

  def selectAll: Query0[Word] =
    sql"""
    SELECT id, phrase, image_url, transcript, english_meaning, russian_translation
    FROM word
  """.query[Word]
}

class DoobieWordRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends WordRepositoryAlgebra[F]
    with IdentityStore[F, Long, Word] {
  self =>

  import WordSQL._

  def create(word: Word): F[Word] =
    insert(word)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => word.copy(id = id.some))
      .transact(xa)

  def update(word: Word): OptionT[F, Word] =
    OptionT.fromOption[F](word.id).semiflatMap { id =>
      WordSQL.update(word, id).run.transact(xa).as(word)
    }

  def get(wordId: Long): OptionT[F, Word] =
    OptionT(select(wordId).option.transact(xa))

  def delete(wordId: Long): OptionT[F, Word] =
    get(wordId).semiflatMap(word =>
      WordSQL.delete(wordId).run.transact(xa).as(word)
    )

  def list(pageSize: Int, offset: Int): F[List[Word]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object DoobieWordRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieWordRepositoryInterpreter[F] =
    new DoobieWordRepositoryInterpreter(xa)
}
