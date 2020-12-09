package io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses.tasks

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.courses.tasks.{Task, TaskService}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  TaskAlreadyExistsError,
  TaskNotFoundError
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

class TaskEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  implicit val classDec: EntityDecoder[F, Task] = jsonOf

  private def createTaskEndpoint(
      taskService: TaskService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action =
        for {
          task <- req.request.as[Task]
          res <- taskService.createTask(task).value
        } yield res

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(TaskAlreadyExistsError(value)) =>
          Conflict(
            s"Given task object already exists at id ${value.id.get}"
          )
      }
  }

  private def deleteTaskEndpoint(
      taskService: TaskService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      taskService.getTask(id).value.flatMap {
        case Right(_) =>
          for {
            _ <- taskService.deleteTask(id)
            res <- Ok()
          } yield res
        case Left(TaskNotFoundError) =>
          NotFound(s"Task not found")
      }
  }

  private def searchTaskEndpoint(
      taskService: TaskService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      taskService.getTask(id).value.flatMap {
        case Right(found)            => Ok(found.asJson)
        case Left(TaskNotFoundError) => NotFound("The task not found")
      }
  }

  def listTasksEndpoint(
      taskService: TaskService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "class" / LongVar(id) asAuthed _ =>
      for {
        retrieved <- taskService.getTasksByClassId(id)
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      taskService: TaskService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val teacherAuthEndpoints: AuthService[F, Auth] =
      Auth.teacherOnly {
        createTaskEndpoint(taskService)
          .orElse(deleteTaskEndpoint(taskService))
      }
    val authEndpoints: AuthService[F, Auth] =
      Auth.allRoles {
        listTasksEndpoint(taskService)
          .orElse(searchTaskEndpoint(taskService))
      }
    auth.liftService(teacherAuthEndpoints) <+> auth.liftService(authEndpoints)
  }
}

object TaskEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      taskService: TaskService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new TaskEndpoints[F, Auth].endpoints(taskService, auth)
}
