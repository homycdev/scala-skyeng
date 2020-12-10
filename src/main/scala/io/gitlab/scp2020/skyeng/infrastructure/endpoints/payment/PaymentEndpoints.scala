package io.gitlab.scp2020.skyeng.infrastructure.endpoints.payment

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import io.gitlab.scp2020.skyeng.domain.StudentNotFoundError
import io.gitlab.scp2020.skyeng.domain.authentication.{Auth, ReplenishRequest}
import io.gitlab.scp2020.skyeng.domain.payment.{
  Transaction,
  TransactionService,
  TransactionStatus
}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.users.student.StudentProfileService
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

import java.time.LocalDateTime

class PaymentEndpoints[F[_]: Sync, Auth: JWTMacAlgo](
    transactionService: TransactionService[F],
    studentProfileService: StudentProfileService[F]
) extends Http4sDsl[F] {
  implicit val replenishDecoder: EntityDecoder[F, ReplenishRequest] = jsonOf

  private def replenishBalanceEndpoint(): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "replenish" asAuthed user =>
      studentProfileService.getStudent(user.id.get).value.flatMap {
        case Right(student) =>
          val checkAmount = {
            for {
              re1 <- req.request.as[ReplenishRequest]
            } yield re1
          }
          checkAmount.flatMap {
            case ReplenishRequest(amount) if amount > 0 =>
              val replenishAction = {
                for {
                  res1 <-
                    studentProfileService
                      .updateStudent(
                        student.copy(balance = student.balance + amount)
                      )
                      .value
                  transaction = Transaction(
                    studentId = student.userId,
                    created = LocalDateTime.now(),
                    status = TransactionStatus.Replenishment,
                    change = amount,
                    reminder = student.balance + amount
                  )
                  res2 <-
                    transactionService.createTransaction(transaction).value
                } yield (res1, res2)
              }

              replenishAction.flatMap {
                case (Right(_), Right(transaction)) => Ok(transaction.asJson)
                case (Left(StudentNotFoundError), _) =>
                  NotFound(s"Student profile at id: ${user.id.get} not found")
                case (_, Left(_)) =>
                  Conflict(s"Error at point of update")
                  NotFound(s"Student profile at id: ${user.id.get} not found")
              }
            case _ => BadRequest("The amount is non-positive")
          }

        case Left(StudentNotFoundError) =>
          NotFound(s"Student profile at id: ${user.id.get} not found")
      }
  }

  private def lessonPassedEndpoint(): AuthEndpoint[F, Auth] = {
    case _ @POST -> Root / "lesson_passed" / LongVar(studentId) asAuthed _ =>
      studentProfileService.getStudent(studentId).value.flatMap {
        case Right(student) =>
          val minusLesson = -1
          val subtractAction = {
            for {
              res1 <-
                studentProfileService
                  .updateStudent(
                    student.copy(balance =
                      Math.max(student.balance + minusLesson, 0)
                    )
                  )
                  .value
              transaction = Transaction(
                studentId = student.userId,
                teacherId = student.teacherId,
                created = LocalDateTime.now(),
                status = TransactionStatus.LessonCompleted,
                change = minusLesson,
                reminder = student.balance + minusLesson
              )
              res2 <- transactionService.createTransaction(transaction).value
            } yield (res1, res2)
          }

          subtractAction.flatMap {
            case (Right(_), Right(transaction)) => Ok(transaction.asJson)
            case (Left(StudentNotFoundError), _) =>
              NotFound(s"Student profile at id: $studentId not found")
            case (_, Left(_)) =>
              Conflict(s"Error at point of update")
              NotFound(s"Student profile at id: $studentId not found")
          }
        case Left(StudentNotFoundError) =>
          NotFound(s"Student profile at id: $studentId not found")
      }
  }

  def listStudentTransactionsEndpoint: AuthEndpoint[F, Auth] = {
    case GET -> Root / "transactions" asAuthed user =>
      studentProfileService.getStudent(user.id.get).value.flatMap {
        case Right(student) =>
          for {
            retrieved <-
              transactionService.getTransactionsByStudentId(student.userId)
            res <- Ok(retrieved.asJson)
          } yield res
        case Left(StudentNotFoundError) =>
          NotFound(s"Student profile at id: ${user.id.get} not found")
      }
  }

  def endpoints(
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] = {
//    val studentAuthEndpoints: AuthService[F, Auth] = {
//      Auth.studentOnly {
//        replenishBalanceEndpoint()
//          .orElse(
//            listStudentTransactionsEndpoint
//          )
//      }
//    }
//    val teacherAuthEndpoints: AuthService[F, Auth] = {
//      Auth.teacherOnly {
//        lessonPassedEndpoint()
//      }
//    }
//    auth.liftService(studentAuthEndpoints) <+> auth.liftService(teacherAuthEndpoints)
    val authEndpoints: AuthService[F, Auth] = {
      Auth.allRoles {
        replenishBalanceEndpoint()
          .orElse(listStudentTransactionsEndpoint)
          .orElse(lessonPassedEndpoint())
      }
    }
    auth.liftService(authEndpoints)
  }
}

object PaymentEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](
      transactionService: TransactionService[F],
      studentProfileService: StudentProfileService[F],
      auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]
  ): HttpRoutes[F] =
    new PaymentEndpoints[F, Auth](transactionService, studentProfileService)
      .endpoints(auth)
}
