package io.gitlab.scp2020.skyeng.domain.schedule

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  ScheduleAlreadyExistsError,
  ScheduleNotFoundError
}

class ScheduleService[F[_]](
    scheduleRepositoryAlgebra: ScheduleRepositoryAlgebra[F]
) {
  def createSchedule(schedule: Schedule)(implicit
      M: Monad[F]
  ): EitherT[F, ScheduleAlreadyExistsError, Schedule] =
    for {
      saved <- EitherT.liftF(scheduleRepositoryAlgebra.create(schedule))
    } yield saved

  def getSchedule(scheduleId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, ScheduleNotFoundError.type, Schedule] =
    scheduleRepositoryAlgebra
      .get(scheduleId)
      .toRight(ScheduleNotFoundError)

  def deleteSchedule(scheduleId: Long)(implicit F: Functor[F]): F[Unit] =
    scheduleRepositoryAlgebra
      .delete(scheduleId)
      .value
      .void

  def updateSchedule(schedule: Schedule)(implicit
      M: Monad[F]
  ): EitherT[F, ScheduleNotFoundError.type, Schedule] =
    for {
      saved <-
        scheduleRepositoryAlgebra
          .update(schedule)
          .toRight(ScheduleNotFoundError)
    } yield saved

  def listSchedules(pageSize: Int, offset: Int): F[List[Schedule]] =
    scheduleRepositoryAlgebra.list(pageSize, offset)

  def getSchedulesByStudentId(studentId: Long): F[List[Schedule]] =
    scheduleRepositoryAlgebra.getByStudentId(studentId)

  def getSchedulesByTeacherId(teacherId: Long): F[List[Schedule]] =
    scheduleRepositoryAlgebra.getByTeacherId(teacherId)
}

object ScheduleService {
  def apply[F[_]](
      scheduleRepositoryAlgebra: ScheduleRepositoryAlgebra[F]
  ): ScheduleService[F] =
    new ScheduleService(scheduleRepositoryAlgebra)
}
