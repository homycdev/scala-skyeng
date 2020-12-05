package io.gitlab.scp2020.skyeng.domain.results

import cats.data.OptionT

trait EnrollmentRepositoryAlgebra[F[_]] {
  def create(enrollment: Enrollment): F[Enrollment]

  def update(enrollment: Enrollment): OptionT[F, Enrollment]

  def get(enrollmentId: Long): OptionT[F, Enrollment]

  def delete(enrollmentId: Long): OptionT[F, Enrollment]

  def list(pageSize: Int, offset: Int): F[List[Enrollment]]

  def getByStudentId(studentId: Long): F[List[Enrollment]]

  def getByCourseId(courseId: Long): F[List[Enrollment]]
}
