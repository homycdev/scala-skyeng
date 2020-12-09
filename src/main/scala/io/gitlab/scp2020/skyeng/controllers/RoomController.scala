package io.gitlab.scp2020.skyeng.controllers

import cats.effect.Sync
import cats.syntax.all._
import io.gitlab.scp2020.skyeng.domain.RoomNotFoundError
import io.gitlab.scp2020.skyeng.domain.schedule.{Room, RoomService}

class RoomController[F[_]: Sync](
    roomService: RoomService[F]
) {
  def setRoomActivity(
      roomId: Long,
      isOpen: Boolean
  ): F[Either[RoomNotFoundError.type, Room]] = {
    val value = roomService.getRoom(roomId).value

    value.flatMap {
      case Right(room) =>
        val action =
          for {
            res <- roomService.updateRoom(room.copy(isOpen = isOpen)).value
          } yield res

        action
      case _ =>
        value
    }
  }

  def getRoomsOfTeacher(teacherId: Long): F[List[Room]] = {
    for {
      retrieved <- roomService.getRoomsByTeacherId(teacherId)
    } yield retrieved
  }

  def getRoomsOfStudent(studentId: Long): F[List[Room]] = {
    for {
      retrieved <- roomService.getRoomsByStudentId(studentId)
    } yield retrieved
  }
}

object RoomController {
  def apply[F[_]: Sync](
      roomService: RoomService[F]
  ): RoomController[F] =
    new RoomController[F](roomService)
}
