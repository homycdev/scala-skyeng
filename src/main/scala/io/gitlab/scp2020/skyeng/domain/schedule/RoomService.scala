package io.gitlab.scp2020.skyeng.domain.schedule

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  RoomAlreadyExistsError,
  RoomNotFoundError
}

class RoomService[F[_]](
    roomRepositoryAlgebra: RoomRepositoryAlgebra[F]
) {
  def createRoom(room: Room)(implicit
      M: Monad[F]
  ): EitherT[F, RoomAlreadyExistsError, Room] =
    for {
      saved <- EitherT.liftF(roomRepositoryAlgebra.create(room))
    } yield saved

  def getRoom(roomId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, RoomNotFoundError.type, Room] =
    roomRepositoryAlgebra
      .get(roomId)
      .toRight(RoomNotFoundError)

  def deleteRoom(roomId: Long)(implicit F: Functor[F]): F[Unit] =
    roomRepositoryAlgebra
      .delete(roomId)
      .value
      .void

  def updateRoom(room: Room)(implicit
      M: Monad[F]
  ): EitherT[F, RoomNotFoundError.type, Room] =
    for {
      saved <-
        roomRepositoryAlgebra
          .update(room)
          .toRight(RoomNotFoundError)
    } yield saved

  def listRooms(pageSize: Int, offset: Int): F[List[Room]] =
    roomRepositoryAlgebra.list(pageSize, offset)

  def getRoomsByStudentId(studentId: Long): F[List[Room]] =
    roomRepositoryAlgebra.getByStudentId(studentId)

  def getRoomsByTeacherId(teacherId: Long): F[List[Room]] =
    roomRepositoryAlgebra.getByTeacherId(teacherId)
}

object RoomService {
  def apply[F[_]](
      roomRepositoryAlgebra: RoomRepositoryAlgebra[F]
  ): RoomService[F] =
    new RoomService(
      roomRepositoryAlgebra
    )
}
