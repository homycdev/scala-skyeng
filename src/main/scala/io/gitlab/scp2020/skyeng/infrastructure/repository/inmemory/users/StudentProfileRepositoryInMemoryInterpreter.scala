package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.users

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits.catsSyntaxApplicativeId
import io.gitlab.scp2020.skyeng.domain.users.student.{
  StudentProfile,
  StudentProfileRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class StudentProfileRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends StudentProfileRepositoryAlgebra[F]
    with IdentityStore[F, Long, StudentProfile] {
  private val cache = new TrieMap[Long, StudentProfile]

  private val random = new Random

  def create(student: StudentProfile): F[StudentProfile] = {
    val id = random.nextLong()
    val toSave = student.copy(userId = id)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(student: StudentProfile): OptionT[F, StudentProfile] = {
    val toSave = student
    cache.update(student.userId, toSave)
    OptionT.liftF(toSave.pure[F])
  }

  def get(id: Long): OptionT[F, StudentProfile] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, StudentProfile] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[StudentProfile]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]
}
object StudentProfileRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]()
  : StudentProfileRepositoryInMemoryInterpreter[F] =
    new StudentProfileRepositoryInMemoryInterpreter[F]
}
