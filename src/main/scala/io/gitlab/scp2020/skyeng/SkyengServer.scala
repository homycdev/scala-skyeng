package io.gitlab.scp2020.skyeng

import cats.effect.{ConcurrentEffect, ContextShift, Resource, Timer, _}
import doobie.util.ExecutionContexts
import io.circe.config.parser
import io.gitlab.scp2020.skyeng.config.{DatabaseConfig, SkyEngConfig}
import io.gitlab.scp2020.skyeng.controllers.UserController
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.users.teacher.{
  TeacherProfileService,
  TeacherProfileValidationInterpreter
}
import io.gitlab.scp2020.skyeng.domain.users.{
  UserService,
  UserValidationInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.{
  TeacherProfileEndpoints,
  UserEndpoints
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.DoobieAuthRepositoryInterpreter
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.users.{
  DoobieTeacherProfileRepositoryInterpreter,
  DoobieUserRepositoryInterpreter
}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server => H4Server}
import tsec.authentication.SecuredRequestHandler
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.jca.BCrypt

object SkyengServer extends IOApp {

  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]
      : Resource[F, H4Server[F]] =
    for {
      // Configs load
      conf <- Resource.liftF(parser.decodePathF[F, SkyEngConfig]("skyeng"))
      serverEc <- ExecutionContexts.cachedThreadPool[F]
      connEc <-
        ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor(
        conf.db,
        connEc,
        Blocker.liftExecutionContext(txnEc)
      )
      key <- Resource.liftF(HMACSHA256.generateKey[F])

      // Repositories init
      userRepo = DoobieUserRepositoryInterpreter[F](xa)
      authRepo = DoobieAuthRepositoryInterpreter[F, HMACSHA256](key, xa)
      teacherRepo = DoobieTeacherProfileRepositoryInterpreter[F](xa)
      // Validations init
      userValidation = UserValidationInterpreter[F](userRepo)
      teacherValidation = TeacherProfileValidationInterpreter[F](teacherRepo)

      // Services init
      userService = UserService[F](userRepo, userValidation)
      teacherService = TeacherProfileService[F](teacherRepo, teacherValidation)

      // Authenticator
      authenticator =
        Auth.jwtAuthenticator[F, HMACSHA256](key, authRepo, userRepo)
      routeAuth = SecuredRequestHandler(authenticator)

      // Controllers
      userController = UserController[F, BCrypt, HMACSHA256](
        userService,
        BCrypt.syncPasswordHasher[F],
        routeAuth.authenticator
      )

      httpApp = Router(
        "/users" -> UserEndpoints
          .endpoints[F, BCrypt, HMACSHA256](
            routeAuth,
            userController
          ),
        "/teachers" -> TeacherProfileEndpoints
          .endpoints[F, HMACSHA256](teacherService, userService, routeAuth)
      ).orNotFound

      _ <- Resource.liftF(DatabaseConfig.initDb(conf.db))

      server <- BlazeServerBuilder[F](serverEc)
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp)
        .resource
    } yield server

  def run(args: List[String]): IO[ExitCode] =
    createServer.use { _ => IO.never }.as(ExitCode.Success)

}
