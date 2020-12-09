package io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses.exercises

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.courses.exercises.{
  Exercise,
  ExerciseService
}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  ExerciseAlreadyExistsError,
  ExerciseNotFoundError
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

class ExerciseEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  implicit val classDec: EntityDecoder[F, Exercise] = jsonOf

  private def createExerciseEndpoint(
      exerciseService: ExerciseService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action =
        for {
          exercise <- req.request.as[Exercise]
          res <- exerciseService.createExercise(exercise).value
        } yield res

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(ExerciseAlreadyExistsError(value)) =>
          Conflict(
            s"Given exercise object already exists at id ${value.id.get}"
          )
      }
  }

  private def deleteExerciseEndpoint(
      exerciseService: ExerciseService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      exerciseService.getExercise(id).value.flatMap {
        case Right(_) =>
          for {
            _ <- exerciseService.deleteExercise(id)
            res <- Ok()
          } yield res
        case Left(ExerciseNotFoundError) =>
          NotFound(s"Exercise not found")
      }
  }

  private def searchExerciseEndpoint(
      exerciseService: ExerciseService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      exerciseService.getExercise(id).value.flatMap {
        case Right(found)                => Ok(found.asJson)
        case Left(ExerciseNotFoundError) => NotFound("The exercise not found")
      }
  }

  def listExercisesEndpoint(
      exerciseService: ExerciseService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "task" / LongVar(id) asAuthed _ =>
      for {
        retrieved <- exerciseService.getExercisesByTaskId(id)
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      exerciseService: ExerciseService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val teacherAuthEndpoints: AuthService[F, Auth] =
      Auth.teacherOnly {
        createExerciseEndpoint(exerciseService)
          .orElse(deleteExerciseEndpoint(exerciseService))
      }
    val authEndpoints: AuthService[F, Auth] =
      Auth.allRoles {
        listExercisesEndpoint(exerciseService)
          .orElse(searchExerciseEndpoint(exerciseService))
      }
    auth.liftService(teacherAuthEndpoints) <+> auth.liftService(authEndpoints)
  }
}

object ExerciseEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      exerciseService: ExerciseService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new ExerciseEndpoints[F, Auth].endpoints(exerciseService, auth)
}
