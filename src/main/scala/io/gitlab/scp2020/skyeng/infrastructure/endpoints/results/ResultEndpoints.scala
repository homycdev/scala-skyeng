package io.gitlab.scp2020.skyeng.infrastructure.endpoints.results

import cats.effect.Sync
import cats.syntax.all._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.{
  Auth,
  ExerciseResultRequest
}
import io.gitlab.scp2020.skyeng.domain.results.{
  ExerciseResult,
  ExerciseResultService
}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  ExerciseResultAlreadyExistsError,
  ExerciseResultNotFoundError
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

class ResultEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {
  implicit val exerciseResultDec: EntityDecoder[F, ExerciseResultRequest] =
    jsonOf

  def countScore(exerciseContent: Json): Int =
    exerciseContent match {
      case _ => 100
    }

  private def createResultsEndpoint(
      exerciseResultService: ExerciseResultService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "exercise" / LongVar(
          exerciseId
        ) asAuthed user =>
      val action =
        for {
          exerciseRes <- req.request.as[ExerciseResultRequest]
          score = countScore(exerciseRes.content)
          result = ExerciseResult(
            studentId = user.id.get,
            exerciseId = exerciseId,
            score = score,
            content = exerciseRes.content
          )
          res <- exerciseResultService.createExerciseResult(result).value
        } yield res

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(ExerciseResultAlreadyExistsError(value)) =>
          Conflict(s"Exercise result already exists: $value")
      }
  }

  private def updateResultsEndpoint(
      exerciseResultService: ExerciseResultService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "exercise" / LongVar(
          exerciseId
        ) asAuthed _ =>
      exerciseResultService.getExerciseResult(exerciseId).value.flatMap {
        case Right(result) =>
          val action =
            for {
              exerciseRes <- req.request.as[ExerciseResultRequest]
              score = countScore(exerciseRes.content)
              res <-
                exerciseResultService
                  .updateExerciseResult(
                    result.copy(content = exerciseRes.content, score = score)
                  )
                  .value
            } yield res

          action.flatMap {
            case Right(saved) => Ok(saved.asJson)
            case Left(_)      => Conflict(s"Error at point of update")
          }
        case Left(ExerciseResultNotFoundError) =>
          NotFound(s"Exercise result at id: $exerciseId not found")
      }
  }

  private def searchExerciseResultsEndpoint(
      exerciseResultService: ExerciseResultService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "exercise" / LongVar(
          exerciseid
        ) asAuthed student =>
      for {
        retrieved <-
          exerciseResultService.getExerciseResultByStudentIdAndExerciseId(
            student.id.get,
            exerciseid
          )
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      exerciseResultService: ExerciseResultService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
//    val studentAuthEndpoints: AuthService[F, Auth] = {
//      Auth.studentOnly {
//        createResultsEndpoint(exerciseResultService)
//          .orElse(updateResultsEndpoint(exerciseResultService))
//          .orElse(searchExerciseResultsEndpoint(exerciseResultService))
//      }
//    }
//    auth.liftService(studentAuthEndpoints)
    val authEndpoints: AuthService[F, Auth] = {
      Auth.allRoles {
        createResultsEndpoint(exerciseResultService)
          .orElse(updateResultsEndpoint(exerciseResultService))
          .orElse(searchExerciseResultsEndpoint(exerciseResultService))
      }
    }
    auth.liftService(authEndpoints)
  }
}

object ResultEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      exerciseResultService: ExerciseResultService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new ResultEndpoints[F, Auth].endpoints(exerciseResultService, auth)
}
