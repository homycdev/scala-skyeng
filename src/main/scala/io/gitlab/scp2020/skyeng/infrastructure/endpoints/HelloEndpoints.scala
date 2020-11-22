//package io.gitlab.scp2020.skyeng.infrastructure.endpoints
//
//import cats.effect.Sync
//import cats.syntax.all._
//import io.circe.generic.auto._
//import io.circe.syntax._
//import org.http4s._
//import org.http4s.circe._
//import org.http4s.dsl.Http4sDsl
//import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, asAuthed}
//import tsec.jwt.algorithms.JWTMacAlgo
//import io.gitlab.scp2020.skyeng.domain.hello.{Hello, HelloService}
//import org.http4s.dsl.io.{GET, POST, Root}
//
//class HelloEndpoints[F[_]: Sync, Auth: JWTMacAlgo] {
//  implicit val helloDecoder: EntityDecoder[F, Hello] = jsonOf
//
//}
