package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.users

import cats.Applicative
import cats.data.OptionT
import cats.implicits.catsSyntaxApplicativeId
import io.gitlab.scp2020.skyeng.domain.users.teacher.{TeacherProfile, TeacherProfileRepositoryAlgebra}

import scala.collection.concurrent.TrieMap
import scala.util.Random

class TeacherProfileRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends TeacherProfileRepositoryAlgebra[F] {
  private val cache = new TrieMap[Long, TeacherProfile]

  private val random = new Random

  def create(teacher: TeacherProfile): F[TeacherProfile] = {
    val id = random.nextLong()
    val toSave = teacher.copy(userId = id)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(teacher: TeacherProfile): OptionT[F, TeacherProfile] = {
      OptionT.liftF(teacher.copy(bio = teacher.bio, greeting = teacher.greeting, qualification = teacher.qualification).pure[F])
  }

  def get(id: Long): OptionT[F, TeacherProfile] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, TeacherProfile] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[TeacherProfile]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]
}
object TeacherProfileRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]()
      : TeacherProfileRepositoryInMemoryInterpreter[F] =
    new TeacherProfileRepositoryInMemoryInterpreter[F]
}
