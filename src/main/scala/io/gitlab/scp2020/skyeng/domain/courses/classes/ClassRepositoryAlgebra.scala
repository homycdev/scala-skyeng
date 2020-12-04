package io.gitlab.scp2020.skyeng.domain.courses.classes

import cats.data.OptionT

trait ClassRepositoryAlgebra[F[_]] {
  def create(classObj: Class): F[Class]

  def update(classObj: Class): OptionT[F, Class]

  def get(classId: Long): OptionT[F, Class]

  def delete(classId: Long): OptionT[F, Class]

  def list(pageSize: Int, offset: Int): F[List[Class]]

  def getByCourseId(courseId: Long): F[List[Class]]
}
