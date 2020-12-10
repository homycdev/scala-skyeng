package io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses.classes

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.{
  Auth,
  LessonUpdateRequest
}
import io.gitlab.scp2020.skyeng.domain.courses.classes.{Lesson, LessonService}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  LessonAlreadyExistsError,
  LessonNotFoundError
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

class LessonEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  implicit val lessonDec: EntityDecoder[F, Lesson] = jsonOf
  implicit val lessonUpdateReqDec: EntityDecoder[F, LessonUpdateRequest] =
    jsonOf

  private def createLessonEndpoint(
      lessonService: LessonService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action =
        for {
          lesson <- req.request.as[Lesson]
          res <- lessonService.createLesson(lesson).value
        } yield res

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(LessonAlreadyExistsError(lesson)) =>
          Conflict(s"Lesson with id: ${lesson.id.get} already exists")
      }
  }

  private def updateLessonEndpoint(
      lessonService: LessonService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ PUT -> Root / LongVar(id) asAuthed _ =>
      lessonService.getLesson(id).value.flatMap {
        case Right(lesson) =>
          val action =
            for {
              request <- req.request.as[LessonUpdateRequest]
              updatable = request.asLesson(lesson)
              res <- lessonService.updateLesson(updatable).value
            } yield res

          action.flatMap {
            case Right(saved) => Ok(saved.asJson)
            case Left(LessonNotFoundError) =>
              NotFound(s"Lesson with id: $id not found")
          }
        case Left(LessonNotFoundError) =>
          NotFound(s"Lesson with id: $id not found")
      }
  }

  private def deleteLessonEndpoint(
      lessonService: LessonService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      lessonService.getLesson(id).value.flatMap {
        case Right(_) =>
          for {
            _ <- lessonService.deleteLesson(id)
            res <- Ok(s"Successfully deleted lesson: $id")
          } yield res
        case Left(LessonNotFoundError) =>
          NotFound(s"Lesson with id: $id not found")
      }
  }

  private def searchLessonEndpoint(
      lessonService: LessonService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      lessonService.getLesson(id).value.flatMap {
        case Right(found)              => Ok(found.asJson)
        case Left(LessonNotFoundError) => NotFound("The lesson not found")
      }
  }

  def listLessonsEndpoint(
      lessonService: LessonService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "course" / LongVar(id) asAuthed _ =>
      for {
        retrieved <- lessonService.getLessonsByCourseId(id)
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      lessonService: LessonService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
//    val teacherAuthEndpoints: AuthService[F, Auth] =
//      Auth.teacherOnly {
//        createLessonEndpoint(lessonService)
//          .orElse(updateLessonEndpoint(lessonService))
//          .orElse(deleteLessonEndpoint(lessonService))
//      }
//    val authEndpoints: AuthService[F, Auth] =
//      Auth.allRoles {
//        listLessonsEndpoint(lessonService)
//          .orElse(searchLessonEndpoint(lessonService))
//      }
//    auth.liftService(teacherAuthEndpoints) <+> auth.liftService(authEndpoints)

    val authEndpoints: AuthService[F, Auth] =
      Auth.allRoles {
        createLessonEndpoint(lessonService)
          .orElse(updateLessonEndpoint(lessonService))
          .orElse(deleteLessonEndpoint(lessonService))
          .orElse(listLessonsEndpoint(lessonService))
          .orElse(searchLessonEndpoint(lessonService))
      }
    auth.liftService(authEndpoints)
  }
}

object LessonEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      lessonService: LessonService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new LessonEndpoints[F, Auth].endpoints(lessonService, auth)

}
