package io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.{
  Auth,
  CourseCreateRequest,
  CourseUpdateRequest
}
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

  implicit val courseUpdateDec: EntityDecoder[F, CourseUpdateRequest] = jsonOf
  implicit val courseCreateDec: EntityDecoder[F, CourseCreateRequest] = jsonOf

  private def createCourseEndpoint(
      courseService: CourseService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action =
        for {
          data <- req.request.as[CourseCreateRequest]
          course = Course(title = data.title, categoryId = data.categoryId)
          created <- courseService.createCourse(course).value
        } yield created

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(CourseAlreadyExistsError(value)) =>
          Conflict(s"Course already exists: $value")
      }

  }

  private def updateCourseEndpoint(
      courseService: CourseService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ PUT -> Root / LongVar(id) asAuthed _ =>
      courseService.getCourse(id).value.flatMap {
        case Right(course) =>
          val action =
            for {
              data <- req.request.as[CourseUpdateRequest]
              updatable = data.asCourse(course)
              res <- courseService.updateCourse(updatable).value
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
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- courseService.deleteCourse(id)
        resp <- Ok()
      } yield resp
  }

  private def searchCourseEndpoint(
      courseService: CourseService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      courseService.getCourse(id).value.flatMap {
        case Right(found)              => Ok(found.asJson)
        case Left(CourseNotFoundError) => NotFound("The course not found")
      }
  }

  def listCoursesEndpoint(
      courseService: CourseService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "category" / LongVar(id) asAuthed _ =>
      for {
        retrieved <- courseService.getCoursesByCategoryId(id)
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      courseService: CourseService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
//    val teacherAuthEndpoints: AuthService[F, Auth] =
//      Auth.teacherOnly {
//        createCourseEndpoint(courseService)
//          .orElse(updateCourseEndpoint(courseService))
//          .orElse(deleteCourseEndpoint(courseService))
//      }
//
//    val authEndpoints: AuthService[F, Auth] =
//      Auth.allRoles {
//        listCoursesEndpoint(courseService)
//          .orElse(searchCourseEndpoint(courseService))
//      }
//    auth.liftService(teacherAuthEndpoints) <+> auth.liftService(authEndpoints)

    val authEndpoints: AuthService[F, Auth] =
      Auth.allRoles {
        createCourseEndpoint(courseService)
          .orElse(updateCourseEndpoint(courseService))
          .orElse(deleteCourseEndpoint(courseService))
          .orElse(listCoursesEndpoint(courseService))
          .orElse(searchCourseEndpoint(courseService))
      }

    auth.liftService(authEndpoints)
  }
}

object CourseEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      courseService: CourseService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new CourseEndpoints[F, Auth].endpoints(courseService, auth)
}
