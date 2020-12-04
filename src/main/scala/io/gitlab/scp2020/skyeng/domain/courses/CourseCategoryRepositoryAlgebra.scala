package io.gitlab.scp2020.skyeng.domain.courses

import cats.data.OptionT

trait CourseCategoryRepositoryAlgebra[F[_]] {
  def create(category: CourseCategory): F[CourseCategory]

  def update(category: CourseCategory): OptionT[F, CourseCategory]

  def get(categoryId: Long): OptionT[F, CourseCategory]

  def delete(categoryId: Long): OptionT[F, CourseCategory]

  def list(pageSize: Int, offset: Int): F[List[CourseCategory]]
}
