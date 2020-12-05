package io.gitlab.scp2020.skyeng.domain.users.student

import cats.data.OptionT

trait StudentProfileRepositoryAlgebra[F[_]] {
  def create(student: StudentProfile): F[StudentProfile]

  def get(studentId: Long): OptionT[F, StudentProfile]

  def update(student: StudentProfile): OptionT[F, StudentProfile]

  def delete(studentId: Long): OptionT[F, StudentProfile]

  def list(pageSize: Int, offset: Int): F[List[StudentProfile]]
}
