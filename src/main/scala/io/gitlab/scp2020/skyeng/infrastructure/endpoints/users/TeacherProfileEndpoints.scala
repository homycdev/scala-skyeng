package io.gitlab.scp2020.skyeng.infrastructure.endpoints.users

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.controllers.RoomController
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.users.teacher.{
  TeacherProfile,
  TeacherProfileService
}
import io.gitlab.scp2020.skyeng.domain.users.{Role, User, UserService}
import io.gitlab.scp2020.skyeng.domain.{
  RoomNotFoundError,
  TeacherAlreadyExistsError,
  TeacherNotFoundError,
  UserNotFoundError
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoint.{
  AuthEndpoint,
  AuthService
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.Pagination.{
  OptionalOffsetMatcher,
  OptionalPageSizeMatcher
}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import tsec.authentication._
import tsec.jwt.algorithms.JWTMacAlgo

class TeacherProfileEndpoints[F[_]: Sync, Auth: JWTMacAlgo]
    extends Http4sDsl[F] {
  implicit val userDecoder: EntityDecoder[F, User] = jsonOf
  implicit val teacherProfileDecoder: EntityDecoder[F, TeacherProfile] = jsonOf

  def endpoints(
      teacherProfileService: TeacherProfileService[F],
      userService: UserService[F],
      roomController: RoomController[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val adminEndpoints: AuthService[F, Auth] =
      Auth.adminOnly {
        setUserAsTeacherEndpoint(teacherProfileService, userService)
          .orElse(listTeacherProfilesEndpoint(teacherProfileService))
          .orElse(searchTeacherProfileEndpoint(teacherProfileService))
          .orElse(deleteTeacherProfileEndpoint(teacherProfileService))

      }
    val teacherEndpoints =
      Auth.teacherOnly {
        updateTeacherProfileEndpoint(teacherProfileService)
          .orElse(getRoomOfTeacherEndpoint(roomController))
          .orElse(setRoomOpenEndpoint(roomController))
          .orElse(setRoomClosedEndpoint(roomController))
      }
    auth.liftService(teacherEndpoints) <+> auth.liftService(adminEndpoints)
  }

  // TODO SET all error messages to the same value as they correspond to error class
  private def setUserAsTeacherEndpoint(
      teacherProfileService: TeacherProfileService[F],
      userService: UserService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "assign" / LongVar(id) asAuthed _ =>
      userService.getUser(id).value.flatMap {
        case Right(foundUser) =>
          val action =
            for {
              teacher <-
                req.request
                  .as[TeacherProfile]
                  .map(_.copy(userId = id))
              saved <- teacherProfileService.createTeacher(teacher).value
            } yield saved

          action.flatMap {
            case Right(saved) =>
              Ok(saved.asJson)
            case Left(TeacherAlreadyExistsError(existing)) =>
              Conflict(
                s"The teacher profile with userId: ${existing.userId} already exists"
              )
          }
          val updatedUser =
            userService.update(foundUser.copy(role = Role("teacher"))).value

          updatedUser.flatMap {
            case Right(saved)            => Ok(saved.asJson)
            case Left(UserNotFoundError) => NotFound("User not found")
          }
        case Left(UserNotFoundError) => NotFound("User not found")
      }

  }

  private def searchTeacherProfileEndpoint(
      teacherProfileService: TeacherProfileService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      teacherProfileService.getTeacher(id).value.flatMap {
        case Right(found)               => Ok(found.asJson)
        case Left(TeacherNotFoundError) => NotFound("The Teacher not found")
      }
  }

  private def updateTeacherProfileEndpoint(
      teacherProfileService: TeacherProfileService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "update" / LongVar(id) asAuthed _ =>
      teacherProfileService.getTeacher(id).value.flatMap {
        case Right(foundTeacher) =>
          val action = {
            for {
              teacher <- req.request.as[TeacherProfile]
              updated = teacher.copy(
                userId = foundTeacher.userId,
                bio = teacher.bio,
                greeting = teacher.greeting,
                qualification = foundTeacher.qualification
              )
              result <- teacherProfileService.updateTeacher(updated).value
            } yield result
          }
          action.flatMap {
            case Right(saved) => Ok(saved.asJson)
            case Left(TeacherNotFoundError) =>
              NotFound(
                s"Teacher with given id: $id not found."
              )
          }

        case Left(TeacherNotFoundError) =>
          NotFound(s"Teacher with given id: $id not found.")
      }

  }

  private def deleteTeacherProfileEndpoint(
      teacherProfileService: TeacherProfileService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- teacherProfileService.deleteTeacher(id)
        resp <- Ok()
      } yield resp
  }

  def listTeacherProfilesEndpoint(
      teacherProfileService: TeacherProfileService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root :? OptionalPageSizeMatcher(
          pageSize
        ) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      for {
        retrieved <- teacherProfileService.list(
          pageSize.getOrElse(10),
          offset.getOrElse(10)
        )
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  private def getRoomOfTeacherEndpoint(
      roomController: RoomController[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "room" asAuthed teacher =>
      for {
        rooms <- roomController.getRoomsOfTeacher(teacher.id.get)
        res <- Ok(rooms.asJson)
      } yield res
  }

  private def setRoomOpenEndpoint(
      roomController: RoomController[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "room/open" / LongVar(roomId) asAuthed _ =>
      val action = roomController.setRoomActivity(roomId, isOpen = true)

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(RoomNotFoundError) =>
          NotFound(s"Room with given id: $roomId not found.")
      }
  }

  private def setRoomClosedEndpoint(
      roomController: RoomController[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "room/open" / LongVar(roomId) asAuthed _ =>
      val action = roomController.setRoomActivity(roomId, isOpen = false)

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(RoomNotFoundError) =>
          NotFound(s"Room with given id: $roomId not found.")
      }
  }
}

object TeacherProfileEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      teacherProfileService: TeacherProfileService[F],
      userService: UserService[F],
      roomController: RoomController[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new TeacherProfileEndpoints[F, Auth]
      .endpoints(teacherProfileService, userService, roomController, auth)
}
