package io.gitlab.scp2020.skyeng.infrastructure.endpoints.users

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.controllers.RoomController
import io.gitlab.scp2020.skyeng.domain.StudentNotFoundError
import io.gitlab.scp2020.skyeng.domain.authentication.{
  Auth,
  StudentUpdateRequest
}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.users.student.StudentProfileService
import io.gitlab.scp2020.skyeng.infrastructure.endpoint.AuthEndpoint
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import tsec.authentication._
import tsec.jwt.algorithms.JWTMacAlgo

class StudentProfileEndpoints[F[_]: Sync, Auth: JWTMacAlgo]
    extends Http4sDsl[F] {

  implicit val userDec: EntityDecoder[F, User] = jsonOf
  implicit val studentDec: EntityDecoder[F, StudentUpdateRequest] = jsonOf

  def endpoints(
      studentProfileService: StudentProfileService[F],
      roomController: RoomController[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
//    val authEndpoints =
//      Auth.adminOnly {
//        deleteStudentProfileEndpoint(studentProfileService)
//          .orElse(searchStudentProfileEndpoint(studentProfileService))
//          .orElse(
//            updateStudentProfileEndpoint(studentProfileService, roomController)
//          )
//      }
//
//    val studentAuthEndpoints =
//      Auth.studentOnly {
//        getRoomOfStudentEndpoint(roomController)
//      }
//    auth.liftService(authEndpoints) <+> auth.liftService(studentAuthEndpoints)
    val authEndpoints =
      Auth.allRoles {
        deleteStudentProfileEndpoint(studentProfileService)
          .orElse(searchStudentProfileEndpoint(studentProfileService))
          .orElse(
            updateStudentProfileEndpoint(studentProfileService, roomController)
          )
          .orElse(getRoomOfStudentEndpoint(roomController))
      }

    auth.liftService(authEndpoints)
  }

  private def searchStudentProfileEndpoint(
      studentService: StudentProfileService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      studentService.getStudent(id).value.flatMap {
        case Right(found)               => Ok(found.asJson)
        case Left(StudentNotFoundError) => NotFound("Student not found")
      }
  }

  private def deleteStudentProfileEndpoint(
      studentProfileService: StudentProfileService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- studentProfileService.deleteStudent(id)
        resp <- Ok()
      } yield resp
  }

  private def updateStudentProfileEndpoint(
      studentProfileService: StudentProfileService[F],
      roomController: RoomController[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ PUT -> Root / LongVar(id) asAuthed _ =>
      val findStudent = for {
        student <- studentProfileService.getStudent(id).value
      } yield student

      findStudent.flatMap {
        case Right(student) =>
          val action = for {
            req <- req.request.as[StudentUpdateRequest]
            updatable = student.copy(teacherId = req.teacherId)
            result <- studentProfileService.updateStudent(updatable).value
            _ <- roomController.assignTeacher(student.userId, req.teacherId)
          } yield result

          action.flatMap {
            case Right(saved) => Ok(saved.asJson)
            case Left(_)      => Conflict("Could not update student.")
          }
        case Left(StudentNotFoundError) =>
          NotFound("Student not found")
      }
  }

  private def getRoomOfStudentEndpoint(
      roomController: RoomController[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root / "room" asAuthed student =>
      for {
        rooms <- roomController.getRoomsOfStudent(student.id.get)
        res <- Ok(rooms.asJson)
      } yield res
  }
}
object StudentProfileEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      studentProfileService: StudentProfileService[F],
      roomController: RoomController[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new StudentProfileEndpoints[F, Auth]
      .endpoints(studentProfileService, roomController, auth)
}
