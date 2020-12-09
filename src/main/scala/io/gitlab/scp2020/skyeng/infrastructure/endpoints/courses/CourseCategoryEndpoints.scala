package io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.courses.{CourseCategoryService, _}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.{
  CourseCategoryAlreadyExistsError,
  CourseCategoryNotFoundError
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoint.{
  AuthEndpoint,
  AuthService
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.Pagination.{
  OptionalOffsetMatcher,
  OptionalPageSizeMatcher
}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, asAuthed}
import tsec.jwt.algorithms.JWTMacAlgo

class CourseCategoryEndpoints[F[_]: Sync, Auth: JWTMacAlgo]
    extends Http4sDsl[F] {

  implicit val courseCategoryDec: EntityDecoder[F, CourseCategory] = jsonOf

  private def createCourseCategoryEndpoint(
      courseCategoryService: CourseCategoryService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action =
        for {
          category <- req.request.as[CourseCategory]
          createdCourseCategory <-
            courseCategoryService.createCourseCategory(category).value
        } yield createdCourseCategory

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(CourseCategoryAlreadyExistsError(value)) =>
          Conflict(s"CourseCategory already exists: $value")
      }

  }

  private def updateCourseCategoryEndpoint(
      courseCategoryService: CourseCategoryService[F]
  ): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / LongVar(id) asAuthed _ =>
      courseCategoryService.getCourseCategory(id).value.flatMap {
        case Right(_) =>
          val action =
            for {
              courseCategory <- req.request.as[CourseCategory]
              res <-
                courseCategoryService.updateCourseCategory(courseCategory).value
            } yield res

          action.flatMap {
            case Right(saved) => Ok(saved.asJson)
            case Left(_)      => Conflict(s"Error at point of update")
          }
        case Left(CourseCategoryNotFoundError) =>
          NotFound(s"CourseCategory at id: $id not found")
      }
  }

  private def deleteCourseCategoryEndpoint(
      courseCategoryService: CourseCategoryService[F]
  ): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- courseCategoryService.deleteCourseCategory(id)
        resp <- Ok()
      } yield resp
  }

  def listCourseCategoriesEndpoint(
      courseCategoryService: CourseCategoryService[F]
  ): AuthEndpoint[F, Auth] = {
    case GET -> Root :? OptionalPageSizeMatcher(
          pageSize
        ) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      for {
        retrieved <- courseCategoryService.listCourseCategories(
          pageSize.getOrElse(10),
          offset.getOrElse(10)
        )
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      courseCategoryService: CourseCategoryService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] = {
      Auth.allRoles {
        createCourseCategoryEndpoint(courseCategoryService)
          .orElse(updateCourseCategoryEndpoint(courseCategoryService))
          .orElse(deleteCourseCategoryEndpoint(courseCategoryService))
      }
    }
    auth.liftService(authEndpoints)
  }
}

object CourseCategoryEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      courseCategoryService: CourseCategoryService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new CourseCategoryEndpoints[F, Auth].endpoints(courseCategoryService, auth)
}
