package io.gitlab.scp2020.skyeng.domain.courses

import cats.data.OptionT

trait CourseRepositoryAlgebra[F[_]] {
  def create(course: Course): F[Course]

  def update(course: Course): OptionT[F, Course]

  def get(courseId: Long): OptionT[F, Course]

  def delete(courseId: Long): OptionT[F, Course]

  def list(pageSize: Int, offset: Int): F[List[Course]]

  def getByCategoryId(categoryId: Long): F[List[Course]]
}
