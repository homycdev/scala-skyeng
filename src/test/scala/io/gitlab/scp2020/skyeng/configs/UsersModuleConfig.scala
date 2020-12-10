package io.gitlab.scp2020.skyeng.configs

import cats.effect.IO
import io.gitlab.scp2020.skyeng.controllers.{RoomController, UserController}
import io.gitlab.scp2020.skyeng.domain.schedule.{
  RoomRepositoryAlgebra,
  RoomService
}
import io.gitlab.scp2020.skyeng.domain.users.student.{
  StudentProfileService,
  StudentProfileValidationAlgebra,
  StudentProfileValidationInterpreter
}
import io.gitlab.scp2020.skyeng.domain.users.teacher.{
  TeacherProfileService,
  TeacherProfileValidationAlgebra,
  TeacherProfileValidationInterpreter
}
import io.gitlab.scp2020.skyeng.domain.users.{
  User,
  UserService,
  UserValidationAlgebra,
  UserValidationInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.users.{
  StudentProfileEndpoints,
  TeacherProfileEndpoints,
  UserEndpoints
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.schedule.RoomRepositoryInMemoryInterpreter
import io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.users.{
  StudentProfileRepositoryInMemoryInterpreter,
  TeacherProfileRepositoryInMemoryInterpreter,
  UserRepositoryInMemoryInterpreter
}
import org.http4s.HttpRoutes
import tsec.authentication.{JWTAuthenticator, SecuredRequestHandler}
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.duration.DurationInt

trait UsersModuleConfig {
  val userRepo: UserRepositoryInMemoryInterpreter[IO] =
    UserRepositoryInMemoryInterpreter[IO]()
  val teacherRepo: TeacherProfileRepositoryInMemoryInterpreter[IO] =
    TeacherProfileRepositoryInMemoryInterpreter[IO]()
  val studentRepo: StudentProfileRepositoryInMemoryInterpreter[IO] =
    StudentProfileRepositoryInMemoryInterpreter[IO]()
  val roomRepo: RoomRepositoryAlgebra[IO] =
    RoomRepositoryInMemoryInterpreter[IO]()

  val userValidation: UserValidationAlgebra[IO] =
    UserValidationInterpreter[IO](userRepo)
  val teacherValidation: TeacherProfileValidationAlgebra[IO] =
    TeacherProfileValidationInterpreter[IO](teacherRepo)
  val studentValidation: StudentProfileValidationAlgebra[IO] =
    StudentProfileValidationInterpreter[IO](studentRepo)

  val userService: UserService[IO] = UserService[IO](userRepo, userValidation)
  val teacherService: TeacherProfileService[IO] =
    TeacherProfileService[IO](teacherRepo, teacherValidation)
  val studentService: StudentProfileService[IO] =
    StudentProfileService[IO](studentRepo, studentValidation)
  val roomService: RoomService[IO] = RoomService[IO](roomRepo)

  val keyO: MacSigningKey[HMACSHA256] = HMACSHA256.unsafeGenerateKey
  val jwtAuth: JWTAuthenticator[IO, Long, User, HMACSHA256] =
    JWTAuthenticator.unbacked.inBearerToken(1.day, None, userRepo, keyO)

  val userController: UserController[IO, BCrypt, HMACSHA256] = UserController(
    userService,
    BCrypt.syncPasswordHasher[IO],
    SecuredRequestHandler(jwtAuth).authenticator
  )

  val roomController: RoomController[IO] = RoomController(
    roomService
  )

  val usersEndpoints: HttpRoutes[IO] = UserEndpoints.endpoints(
    SecuredRequestHandler(jwtAuth),
    userController,
    roomController,
    teacherService,
    studentService
  )
  val teacherEndpoints: HttpRoutes[IO] =
    TeacherProfileEndpoints.endpoints[IO, HMACSHA256](
      teacherService,
      roomController,
      SecuredRequestHandler(jwtAuth)
    )
  val studentEndpoints: HttpRoutes[IO] =
    StudentProfileEndpoints.endpoints[IO, HMACSHA256](
      studentService,
      roomController,
      SecuredRequestHandler(jwtAuth)
    )
}
object UsersModuleConfig extends UsersModuleConfig
