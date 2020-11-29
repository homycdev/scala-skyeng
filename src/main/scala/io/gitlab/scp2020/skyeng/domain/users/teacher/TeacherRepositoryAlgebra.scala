package io.gitlab.scp2020.skyeng.domain.users.teacher

import cats.data.OptionT
import io.gitlab.scp2020.skyeng.domain.users.User

trait TeacherRepositoryAlgebra[F[_]] {
  def create(potentialTeacher: User, newTeacher: TeacherProfile): F[TeacherProfile]

  def update(teacher: TeacherProfile): F[TeacherProfile]

  def get(teacherId: Long): OptionT[F, TeacherProfile]

  def delete(teacherId: Long): OptionT[F, TeacherProfile]

  def list(pageSize: Int, offset: Int): F[List[TeacherProfile]]


}
