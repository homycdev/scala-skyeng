package io.gitlab.scp2020.skyeng.domain.users.student

import cats.data.OptionT
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.users.teacher.TeacherProfile

trait StudentRepositoryAlgebra[F[_]] {
  def create(potentialStudent: User): F[StudentProfile]

  def update(student: StudentProfile): F[StudentProfile]

  def get(studentId: Long): OptionT[F, StudentProfile]

  def delete(studentId: Long): OptionT[F, StudentProfile]

  def getTeacher(studentId: Long): OptionT[F, TeacherProfile]

  def getBalance(studentId: Long): OptionT[F, Int]

  def list(pageSize: Int, offset: Int): F[List[StudentProfile]]

}
