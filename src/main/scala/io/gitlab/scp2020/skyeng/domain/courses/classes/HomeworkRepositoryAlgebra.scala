package io.gitlab.scp2020.skyeng.domain.courses.classes

import cats.data.OptionT

trait HomeworkRepositoryAlgebra[F[_]] {
  def create(homework: Homework): F[Homework]

  def update(homework: Homework): OptionT[F, Homework]

  def get(homeworkId: Long): OptionT[F, Homework]

  def delete(homeworkId: Long): OptionT[F, Homework]

  def list(pageSize: Int, offset: Int): F[List[Homework]]

  def getByCourseId(courseId: Long): F[List[Homework]]
}
