package io.gitlab.scp2020.skyeng.domain.schedule

import cats.data.OptionT

trait RoomRepositoryAlgebra[F[_]] {
  def create(room: Room): F[Room]

  def update(room: Room): OptionT[F, Room]

  def get(roomId: Long): OptionT[F, Room]

  def delete(roomId: Long): OptionT[F, Room]

  def list(pageSize: Int, offset: Int): F[List[Room]]

  def getByStudentId(studentId: Long): F[List[Room]]

  def getByTeacherId(teacherId: Long): F[List[Room]]
}
