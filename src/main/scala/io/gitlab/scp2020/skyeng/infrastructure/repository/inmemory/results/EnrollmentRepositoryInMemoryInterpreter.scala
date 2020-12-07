package io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.results

import java.util.Random

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import io.gitlab.scp2020.skyeng.domain.courses.{
  Enrollment,
  EnrollmentRepositoryAlgebra
}
import tsec.authentication.IdentityStore

import scala.collection.concurrent.TrieMap

class EnrollmentRepositoryInMemoryInterpreter[F[_]: Applicative]
    extends EnrollmentRepositoryAlgebra[F]
    with IdentityStore[F, Long, Enrollment] {
  private val cache = new TrieMap[Long, Enrollment]

  private val random = new Random

  def create(enrollment: Enrollment): F[Enrollment] = {
    val id = random.nextLong()
    val toSave = enrollment.copy(id = id.some)
    cache += (id -> toSave)
    toSave.pure[F]
  }

  def update(enrollment: Enrollment): OptionT[F, Enrollment] =
    OptionT {
      enrollment.id.traverse { id =>
        cache.update(id, enrollment)
        enrollment.pure[F]
      }
    }

  def get(id: Long): OptionT[F, Enrollment] =
    OptionT.fromOption(cache.get(id))

  def delete(id: Long): OptionT[F, Enrollment] =
    OptionT.fromOption(cache.remove(id))

  def list(pageSize: Int, offset: Int): F[List[Enrollment]] =
    cache.values.toList
      .slice(offset, offset + pageSize)
      .pure[F]

  def getByStudentId(studentId: Long): F[List[Enrollment]] =
    cache.values.filter(u => u.studentId == studentId).toList.pure[F]

  def getByCourseId(courseId: Long): F[List[Enrollment]] =
    cache.values.filter(u => u.courseId == courseId).toList.pure[F]
}

object EnrollmentRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() =
    new EnrollmentRepositoryInMemoryInterpreter[F]
}
