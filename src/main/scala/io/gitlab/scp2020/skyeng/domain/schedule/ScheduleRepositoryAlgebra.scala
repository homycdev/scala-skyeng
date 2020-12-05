package io.gitlab.scp2020.skyeng.domain.schedule

import cats.data.OptionT

trait ScheduleRepositoryAlgebra[F[_]] {
  def create(schedule: Schedule): F[Schedule]

  def update(schedule: Schedule): OptionT[F, Schedule]

  def get(scheduleId: Long): OptionT[F, Schedule]

  def delete(scheduleId: Long): OptionT[F, Schedule]

  def list(pageSize: Int, offset: Int): F[List[Schedule]]

  def getByStudentId(studentId: Long): F[List[Schedule]]

  def getByTeacherId(teacherId: Long): F[List[Schedule]]
}
