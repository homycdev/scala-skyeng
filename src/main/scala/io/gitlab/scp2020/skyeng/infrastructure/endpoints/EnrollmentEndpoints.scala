package io.gitlab.scp2020.skyeng.infrastructure.endpoints

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.courses.{Enrollment, EnrollmentService}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  EnrollmentAlreadyExistsError,
  EnrollmentNotFoundError
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

class EnrollmentEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  implicit val enrollmentDec: EntityDecoder[F, Enrollment] = jsonOf

  private def enrollEndpoint(
      enrollmentService: EnrollmentService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "enroll" / "course" / LongVar(id) asAuthed user =>
      val action =
        // Payload is needed to prevent user from his/her stupidity by putting wrong user id or course id
        for {
          enrollment <- req.request.as[Enrollment]
          payload = enrollment.copy(
            id = user.id,
            studentId = user.id.get,
            courseId = id
          ) // TODO this is very stupid, but straightforward approach. Cant think of nothing better atm.
          res <- enrollmentService.createEnrollment(payload).value
        } yield res

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(EnrollmentAlreadyExistsError(existing)) =>
          Conflict(
            s"User: ${existing.studentId}, enrolled to the course: ${existing.courseId}, with enrollmentId: ${existing.id}"
          )
      }
  }

  private def deleteEnrollmentEndpoint(
      enrollmentService: EnrollmentService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      enrollmentService.getEnrollment(id).value.flatMap {
        case Right(_) =>
          for {
            _ <- enrollmentService.deleteEnrollment(id)
            resp <- Ok()
          } yield resp
        case Left(EnrollmentNotFoundError) =>
          NotFound(s"Enrollment: $id not found...")
      }
  }

  def endpoints(
      enrollmentService: EnrollmentService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val teacherAuthEndpoints: AuthService[F, Auth] =
      Auth.teacherOnly {
        enrollEndpoint(enrollmentService)
          .orElse(deleteEnrollmentEndpoint(enrollmentService))
      }
    auth.liftService(teacherAuthEndpoints)
  }

}

object EnrollmentEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      enrollmentService: EnrollmentService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new EnrollmentEndpoints[F, Auth]
      .endpoints(enrollmentService, auth)

}
