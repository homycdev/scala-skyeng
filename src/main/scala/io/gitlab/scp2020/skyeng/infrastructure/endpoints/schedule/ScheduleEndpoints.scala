package io.gitlab.scp2020.skyeng.infrastructure.endpoints.schedule

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.schedule.{Schedule, ScheduleService}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  ScheduleAlreadyExistsError,
  ScheduleNotFoundError
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoint.{
  AuthEndpoint,
  AuthService
}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, asAuthed}
import tsec.jwt.algorithms.JWTMacAlgo

class ScheduleEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {
  implicit val scheduleDecoder: EntityDecoder[F, Schedule] = jsonOf

  private def createScheduleEndpoint(
      scheduleService: ScheduleService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action =
        for {
          schedule <- req.request.as[Schedule]
          createdCourse <- scheduleService.createSchedule(schedule).value
        } yield createdCourse

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(ScheduleAlreadyExistsError(value)) =>
          Conflict(s"Exercise result already exists: $value")
      }
  }

  private def updateCourseEndpoint(
      scheduleService: ScheduleService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / LongVar(id) asAuthed _ =>
      scheduleService.getSchedule(id).value.flatMap {
        case Right(_) =>
          val action =
            for {
              schedule <- req.request.as[Schedule]
              res <- scheduleService.updateSchedule(schedule).value
            } yield res

          action.flatMap {
            case Right(saved) => Ok(saved.asJson)
            case Left(_)      => Conflict(s"Error at point of update")
          }
        case Left(ScheduleNotFoundError) =>
          NotFound(s"Schedule at id: $id not found")
      }
  }

  private def deleteCourseEndpoint(
      scheduleService: ScheduleService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- scheduleService.deleteSchedule(id)
        resp <- Ok()
      } yield resp
  }

  private def searchScheduleOfStudentEndpoint(
      scheduleService: ScheduleService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root asAuthed student =>
      for {
        retrieved <- scheduleService.getSchedulesByStudentId(student.id.get)
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  private def searchScheduleOfTeacherEndpoint(
      scheduleService: ScheduleService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root asAuthed teacher =>
      for {
        retrieved <- scheduleService.getSchedulesByTeacherId(teacher.id.get)
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      scheduleService: ScheduleService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val studentAuthEndpoints: AuthService[F, Auth] = {
      Auth.studentOnly {
        searchScheduleOfStudentEndpoint(scheduleService)
      }
    }
    val teacherAuthEndpoints: AuthService[F, Auth] = {
      Auth.teacherOnly {
        searchScheduleOfTeacherEndpoint(scheduleService)
          .orElse(createScheduleEndpoint(scheduleService))
          .orElse(updateCourseEndpoint(scheduleService))
          .orElse(deleteCourseEndpoint(scheduleService))
      }
    }
    auth.liftService(studentAuthEndpoints) <+> auth.liftService(
      teacherAuthEndpoints
    )
  }
}

object ScheduleEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      scheduleService: ScheduleService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new ScheduleEndpoints[F, Auth].endpoints(scheduleService, auth)
}
