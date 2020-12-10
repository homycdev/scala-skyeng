package io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.authentication.{
  Auth,
  CourseCategoryRequest
}
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

class CourseCategoryEndpoints[F[_]: Sync, Auth: JWTMacAlgo](
    courseCategoryService: CourseCategoryService[F]
) extends Http4sDsl[F] {

  implicit val courseCategoryDec: EntityDecoder[F, CourseCategoryRequest] =
    jsonOf

  private def createCourseCategoryEndpoint(): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action =
        for {
          data <- req.request.as[CourseCategoryRequest]
          category = CourseCategory(title = data.title)
          createdCourseCategory <-
            courseCategoryService.createCourseCategory(category).value
        } yield createdCourseCategory

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(CourseCategoryAlreadyExistsError(value)) =>
          Conflict(s"CourseCategory already exists: $value")
      }

  }

  private def updateCourseCategoryEndpoint(): AuthEndpoint[F, Auth] = {
    case req @ PUT -> Root / LongVar(id) asAuthed _ =>
      courseCategoryService.getCourseCategory(id).value.flatMap {
        case Right(category) =>
          val action =
            for {
              data <- req.request.as[CourseCategoryRequest]
              res <-
                courseCategoryService
                  .updateCourseCategory(category.copy(title = data.title))
                  .value
            } yield res

          action.flatMap {
            case Right(saved) => Ok(saved.asJson)
            case Left(_)      => Conflict(s"Error at point of update")
          }
        case Left(CourseCategoryNotFoundError) =>
          NotFound(s"CourseCategory at id: $id not found")
      }
  }

  private def deleteCourseCategoryEndpoint(): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- courseCategoryService.deleteCourseCategory(id)
        resp <- Ok()
      } yield resp
  }

  def listCourseCategoriesEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root :? OptionalPageSizeMatcher(
          pageSize
        ) :? OptionalOffsetMatcher(offset) asAuthed _ =>
      for {
        retrieved <- courseCategoryService.listCourseCategories(
          pageSize.getOrElse(10),
          offset.getOrElse(0)
        )
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

  def endpoints(
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
//    val authEndpoints: AuthService[F, Auth] = {
//      Auth.allRoles {
//        updateCourseCategoryEndpoint()
//          .orElse(deleteCourseCategoryEndpoint())
//          .orElse(listCourseCategoriesEndpoint)
//      }
//    }
//    val adminAuthEndpoints = Auth.adminOnly {
//      createCourseCategoryEndpoint()
//    }
//    auth.liftService(adminAuthEndpoints) <+> auth.liftService(authEndpoints)

    val authEndpoints: AuthService[F, Auth] = {
      Auth.allRoles {
        updateCourseCategoryEndpoint()
          .orElse(deleteCourseCategoryEndpoint())
          .orElse(listCourseCategoriesEndpoint)
          .orElse(createCourseCategoryEndpoint())
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
    new CourseCategoryEndpoints[F, Auth](courseCategoryService).endpoints(auth)
}
