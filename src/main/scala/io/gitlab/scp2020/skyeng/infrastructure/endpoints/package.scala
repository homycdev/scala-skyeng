package io.gitlab.scp2020.skyeng.infrastructure

import io.gitlab.scp2020.skyeng.domain.users.User
import org.http4s.Response
import tsec.authentication.{AugmentedJWT, SecuredRequest, TSecAuthService}


package object endpoint {
  type AuthService[F[_], Auth] = TSecAuthService[User, AugmentedJWT[Auth, Long], F]
  type AuthEndpoint[F[_], Auth] =
    PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, Long]], F[Response[F]]]
}
