package io.gitlab.scp2020.skyeng.domain.users

import java.time.LocalDateTime

import cats.Applicative
import tsec.authorization.AuthorizationInfo

case class User(
                 userName: String,
                 firstName: Option[String] = None,
                 lastName: Option[String] = None,
                 birthDate: Option[String] = None,
                 gender: Option[String] = None,
                 email: String,
                 hash: String,
                 phone: Option[String] = None,
                 role: Role = Role("Student"),
                 created: LocalDateTime,
                 id: Option[Long] = None,
               )

object User {
  implicit def authRol[F[_]](implicit F: Applicative[F]): AuthorizationInfo[F, Role, User] = {
    new AuthorizationInfo[F, Role, User] {
      override def fetchInfo(u: User): F[Role] = F.pure(u.role)
    }
  }
}