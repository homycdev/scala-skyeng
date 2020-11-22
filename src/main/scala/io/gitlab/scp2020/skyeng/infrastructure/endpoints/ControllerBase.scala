//package io.gitlab.scp2020.skyeng.infrastructure.endpoints
//
//import cats.effect.IO
//import sttp.model.StatusCode
//import sttp.tapir.{endpoint, statusCode, stringBody, _}
//
//abstract class ControllerBase {
//  val baseEndpoint = endpoint
//    .in("api" / "v1")
//    .errorOut(statusCode.and(stringBody))
//
//  def withStatus[A](f: IO[A]): IO[Either[(StatusCode, String), A]] =
//    f.attempt.map(x => x match {
//      case Right(value) => Right(value)
//      case Left(value) => Left(StatusCode.InternalServerError, value.getMessage)
//    })
//}
