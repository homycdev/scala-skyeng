package io.gitlab.scp2020.skyeng.domain.courses.classes

import cats.data.OptionT

trait LessonRepositoryAlgebra[F[_]] {
  def create(lesson: Lesson): F[Lesson]

  def update(lesson: Lesson): OptionT[F, Lesson]

  def get(lessonId: Long): OptionT[F, Lesson]

  def delete(lessonId: Long): OptionT[F, Lesson]

  def list(pageSize: Int, offset: Int): F[List[Lesson]]

  def getByCourseId(courseId: Long): F[List[Lesson]]
}
