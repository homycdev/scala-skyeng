package io.gitlab.scp2020.skyeng.domain.results

import cats.data.OptionT

trait ClassResultRepositoryAlgebra[F[_]] {
  def create(result: ClassResult): F[ClassResult]

  def update(result: ClassResult): OptionT[F, ClassResult]

  def get(resultId: Long): OptionT[F, ClassResult]

  def delete(resultId: Long): OptionT[F, ClassResult]

  def list(pageSize: Int, offset: Int): F[List[ClassResult]]

  def getByStudentId(studentId: Long): F[List[ClassResult]]

  def getByClassId(classId: Long): F[List[ClassResult]]
}
