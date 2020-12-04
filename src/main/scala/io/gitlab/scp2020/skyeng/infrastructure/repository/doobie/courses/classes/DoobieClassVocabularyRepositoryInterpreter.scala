package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.classes

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie._
import doobie.implicits.{toSqlInterpolator, _}
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  ClassVocabulary,
  ClassVocabularyRepositoryAlgebra
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import tsec.authentication.IdentityStore

private object ClassVocabularySQL {
  def insert(vocabulary: ClassVocabulary): Update0 =
    sql"""
    INSERT INTO class_vocabulary (word_id, class_id)
    VALUES (${vocabulary.wordId}, ${vocabulary.classId})
  """.update

  def update(vocabulary: ClassVocabulary, id: Long): Update0 =
    sql"""
    UPDATE class_vocabulary
    SET word_id = ${vocabulary.wordId}, class_id = ${vocabulary.classId} 
    WHERE id = $id
  """.update

  def select(vocabularyId: Long): Query0[ClassVocabulary] =
    sql"""
    SELECT id, word_id, class_id
    FROM class_vocabulary
    WHERE id = $vocabularyId
  """.query[ClassVocabulary]

  def delete(vocabularyId: Long): Update0 =
    sql"""
    DELETE FROM class_vocabulary WHERE id = $vocabularyId
  """.update

  def selectAll: Query0[ClassVocabulary] =
    sql"""
    SELECT id, word_id, class_id
    FROM class_vocabulary
  """.query[ClassVocabulary]

  def selectByClassId(classId: Long): Query0[ClassVocabulary] =
    sql"""
    SELECT id, word_id, class_id
    FROM class_vocabulary
    WHERE class_id = $classId
  """.query[ClassVocabulary]
}

class DoobieClassVocabularyRepositoryInterpreter[
    F[_]: Bracket[*[_], Throwable]
](
    val xa: Transactor[F]
) extends ClassVocabularyRepositoryAlgebra[F]
    with IdentityStore[F, Long, ClassVocabulary] {
  self =>

  import ClassVocabularySQL._

  def create(vocabularyObj: ClassVocabulary): F[ClassVocabulary] =
    insert(vocabularyObj)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => vocabularyObj.copy(id = id.some))
      .transact(xa)

  def update(vocabularyObj: ClassVocabulary): OptionT[F, ClassVocabulary] =
    OptionT.fromOption[F](vocabularyObj.id).semiflatMap { id =>
      ClassVocabularySQL
        .update(vocabularyObj, id)
        .run
        .transact(xa)
        .as(vocabularyObj)
    }

  def get(vocabularyId: Long): OptionT[F, ClassVocabulary] =
    OptionT(select(vocabularyId).option.transact(xa))

  def delete(vocabularyId: Long): OptionT[F, ClassVocabulary] =
    get(vocabularyId).semiflatMap(vocabularyObj =>
      ClassVocabularySQL.delete(vocabularyId).run.transact(xa).as(vocabularyObj)
    )

  def list(pageSize: Int, offset: Int): F[List[ClassVocabulary]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def getByClassId(classId: Long): F[List[ClassVocabulary]] =
    selectByClassId(classId).to[List].transact(xa)
}

object DoobieClassVocabularyRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieClassVocabularyRepositoryInterpreter[F] =
    new DoobieClassVocabularyRepositoryInterpreter(xa)
}
