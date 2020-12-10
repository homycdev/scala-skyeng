package io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses.classes

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.courses.classes._
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  HomeworkAlreadyExistsError,
  HomeworkNotFoundError
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

class HomeworkEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  implicit val classDec: EntityDecoder[F, Homework] = jsonOf

  private def createHomeworkEndpoint(
      homeworkService: HomeworkService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action =
        for {
          homework <- req.request.as[Homework]
          res <- homeworkService.createHomework(homework).value
        } yield res

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(HomeworkAlreadyExistsError(value)) =>
          Conflict(
            s"Given homework object already exists at id ${value.id.get}"
          )
      }
  }

  private def deleteHomeworkEndpoint(
      homeworkService: HomeworkService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      homeworkService.getHomework(id).value.flatMap {
        case Right(_) =>
          for {
            _ <- homeworkService.deleteHomework(id)
            res <- Ok()
          } yield res
        case Left(HomeworkNotFoundError) =>
          NotFound(s"Homework not found")
      }
  }

  private def searchHomeworkEndpoint(
      homeworkService: HomeworkService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      homeworkService.getHomework(id).value.flatMap {
        case Right(found)                => Ok(found.asJson)
        case Left(HomeworkNotFoundError) => NotFound("The homework not found")
      }
  }

  def listHomeworksEndpoint(
      homeworkService: HomeworkService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "course" / LongVar(id) asAuthed _ =>
      for {
        retrieved <- homeworkService.getHomeworksByCourseId(id)
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      homeworkService: HomeworkService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
//    val teacherAuthEndpoints: AuthService[F, Auth] =
//      Auth.teacherOnly {
//        createHomeworkEndpoint(homeworkService)
//          .orElse(deleteHomeworkEndpoint(homeworkService))
//      }
//    val authEndpoints: AuthService[F, Auth] =
//      Auth.allRoles {
//        listHomeworksEndpoint(homeworkService)
//          .orElse(searchHomeworkEndpoint(homeworkService))
//      }
//    auth.liftService(teacherAuthEndpoints) <+> auth.liftService(authEndpoints)

    val authEndpoints: AuthService[F, Auth] =
      Auth.allRoles {
        listHomeworksEndpoint(homeworkService)
          .orElse(searchHomeworkEndpoint(homeworkService))
          .orElse(deleteHomeworkEndpoint(homeworkService))
          .orElse(createHomeworkEndpoint(homeworkService))
      }
    auth.liftService(authEndpoints)
  }
}

object HomeworkEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      homeworkService: HomeworkService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new HomeworkEndpoints[F, Auth].endpoints(homeworkService, auth)
}
