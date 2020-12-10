package io.gitlab.scp2020.skyeng.controllers

import cats.effect.Sync
import cats.syntax.all._
import io.gitlab.scp2020.skyeng.domain.schedule.{Room, RoomService}
import io.gitlab.scp2020.skyeng.domain.{
  RoomAlreadyExistsError,
  RoomNotFoundError
}

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

  def assignTeacher(
      studentId: Long,
      teacherId: Option[Long]
  ): F[List[Room]] = {
    val value = roomService.getRoomsByStudentId(studentId)

    value.flatMap {
      case list: List[Room] =>
        list.headOption match {
          case Some(room) =>
            for {
              res <-
                roomService.updateRoom(room.copy(teacherId = teacherId)).value
            } yield res
            value
          case _ => value
        }
      case _ => value
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

  def createRoom(
      studentId: Long
  ): F[Either[RoomAlreadyExistsError, Room]] = {
    val room = Room(studentId = studentId, url = "url")
    for {
      created <- roomService.createRoom(room).value
    } yield created
  }
}

object RoomController {
  def apply[F[_]: Sync](
      roomService: RoomService[F]
  ): RoomController[F] =
    new RoomController[F](roomService)
}
