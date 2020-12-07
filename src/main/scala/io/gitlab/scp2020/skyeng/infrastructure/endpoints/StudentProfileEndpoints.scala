package io.gitlab.scp2020.skyeng.infrastructure.endpoints

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.StudentAlreadyExistsError
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.users.student.{StudentProfile, StudentProfileService}
import io.gitlab.scp2020.skyeng.infrastructure.endpoint.AuthEndpoint
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, asAuthed}
import tsec.jwt.algorithms.JWTMacAlgo

// TODO work in progress. Need to implement CourseService
class StudentProfileEndpoints[F[_]: Sync, Auth: JWTMacAlgo]
    extends Http4sDsl[F] {
  implicit val userDec: EntityDecoder[F, User] = jsonOf
  implicit val studentDec: EntityDecoder[F, StudentProfile] = jsonOf

  def endpoints(
      studentProfileService: StudentProfileService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val authEndpoints =
      Auth.allRolesHandler(
        setStudentProfileEndpoint(studentProfileService)
      ) {
        Auth.adminOnly(deleteStudentProfileEndpoint(studentProfileService))
      }
    auth.liftService(authEndpoints)
  }

  private def setStudentProfileEndpoint(
      studentProfileService: StudentProfileService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed user =>
      val action =
        for {
          student <-
            req.request
              .as[StudentProfile]
              .map(_.copy(userId = user.id match {
                case Some(value) => value
                case None =>
                  throw new Exception(
                    "Impossible to set student to non existing user."
                  )
              }))
          saved <- studentProfileService.createStudent(student).value
        } yield saved

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(StudentAlreadyExistsError(existing)) =>
          Conflict(
            s"The Student profile with userId: ${existing.userId} already exists"
          )

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

}
