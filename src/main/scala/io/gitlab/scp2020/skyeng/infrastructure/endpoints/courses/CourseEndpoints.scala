package io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.courses.{CourseService, _}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  CourseAlreadyExistsError,
  CourseNotFoundError
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

class CourseEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  implicit val courseDec: EntityDecoder[F, Course] = jsonOf

  private def createCourseEndpoint(
      courseService: CourseService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "course" asAuthed _ =>
      val action =
        for {
          course <- req.request.as[Course]
          createdCourse <- courseService.createCourse(course).value
        } yield createdCourse

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(CourseAlreadyExistsError(value)) =>
          Conflict(s"Course already exists: $value")
      }

  }

  private def updateCourseEndpoint(
      courseService: CourseService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "course" / LongVar(id) asAuthed _ =>
      courseService.getCourse(id).value.flatMap {
        case Right(_) =>
          val action =
            for {
              course <- req.request.as[Course]
              res <- courseService.updateCourse(course).value
            } yield res

          action.flatMap {
            case Right(saved) => Ok(saved.asJson)
            case Left(_)      => Conflict(s"Error at point of update")
          }
        case Left(CourseNotFoundError) =>
          NotFound(s"Course at id: $id not found")
      }
  }

  private def deleteCourseEndpoint(
      courseService: CourseService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / "course" / LongVar(id) asAuthed _ =>
      for {
        _ <- courseService.deleteCourse(id)
        resp <- Ok()
      } yield resp
  }

  def endpoints(
      courseService: CourseService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val teacherAuthEndpoints: AuthService[F, Auth] = {
      Auth.teacherOnly {
        createCourseEndpoint(courseService)
          .orElse(updateCourseEndpoint(courseService))
          .orElse(deleteCourseEndpoint(courseService))
      }
    }
    auth.liftService(teacherAuthEndpoints)
  }
}

object CourseEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      courseService: CourseService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new CourseEndpoints[F, Auth].endpoints(courseService, auth)
}
