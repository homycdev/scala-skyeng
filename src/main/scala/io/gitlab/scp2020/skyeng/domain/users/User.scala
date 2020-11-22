package io.gitlab.scp2020.skyeng.domain.users

import cats.Applicative
import tsec.authorization.AuthorizationInfo

case class User(
                 userName: String,
                 firstName: String,
                 lastName: String,
                 email: String,
                 hash: String,
                 phone: String,
                 id: Option[Long] = None,
                 role: Role,
               )

//TODO Extend as they should be

object User {
  implicit def authRol[F[_]](implicit F: Applicative[F]): AuthorizationInfo[F, Role, User] = {
    new AuthorizationInfo[F, Role, User] {
      override def fetchInfo(u: User): F[Role] = F.pure(u.role)
    }
  }
}