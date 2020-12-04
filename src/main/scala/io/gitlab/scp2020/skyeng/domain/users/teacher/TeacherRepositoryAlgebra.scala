package io.gitlab.scp2020.skyeng.domain.users.teacher

import cats.data.OptionT

trait TeacherRepositoryAlgebra[F[_]] {
  def create(teacher: TeacherProfile): F[TeacherProfile]

  def update(teacher: TeacherProfile): OptionT[F, TeacherProfile]

  def get(teacherId: Long): OptionT[F, TeacherProfile]

  def delete(teacherId: Long): OptionT[F, TeacherProfile]

//  def list(pageSize: Int, offset: Int): F[List[TeacherProfile]]


}
