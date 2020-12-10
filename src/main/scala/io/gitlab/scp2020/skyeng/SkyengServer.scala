package io.gitlab.scp2020.skyeng

import cats.effect.{ConcurrentEffect, ContextShift, Resource, Timer, _}
import doobie.util.ExecutionContexts
import io.circe.config.parser
import io.gitlab.scp2020.skyeng.config.{DatabaseConfig, SkyEngConfig}
import io.gitlab.scp2020.skyeng.controllers.{RoomController, UserController}
import io.gitlab.scp2020.skyeng.domain.authentication.Auth
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  HomeworkService,
  LessonService
}
import io.gitlab.scp2020.skyeng.domain.courses.exercises.ExerciseService
import io.gitlab.scp2020.skyeng.domain.courses.tasks.TaskService
import io.gitlab.scp2020.skyeng.domain.courses.{
  CourseCategoryService,
  CourseService,
  EnrollmentService
}
import io.gitlab.scp2020.skyeng.domain.payment.TransactionService
import io.gitlab.scp2020.skyeng.domain.results.ExerciseResultService
import io.gitlab.scp2020.skyeng.domain.schedule.{RoomService, ScheduleService}
import io.gitlab.scp2020.skyeng.domain.users.student.{
  StudentProfileService,
  StudentProfileValidationInterpreter
}
import io.gitlab.scp2020.skyeng.domain.users.teacher.{
  TeacherProfileService,
  TeacherProfileValidationInterpreter
}
import io.gitlab.scp2020.skyeng.domain.users.{
  UserService,
  UserValidationInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses.classes.{
  HomeworkEndpoints,
  LessonEndpoints
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses.exercises.ExerciseEndpoints
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses.tasks.TaskEndpoints
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.courses.{
  CourseCategoryEndpoints,
  CourseEndpoints,
  EnrollmentEndpoints
}
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.payment.PaymentEndpoints
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.results.ResultEndpoints
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.schedule.ScheduleEndpoints
import io.gitlab.scp2020.skyeng.infrastructure.endpoints.users.{
  StudentProfileEndpoints,
  TeacherProfileEndpoints,
  UserEndpoints
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.DoobieAuthRepositoryInterpreter
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.classes.{
  DoobieHomeworkRepositoryInterpreter,
  DoobieLessonRepositoryInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.exercises.DoobieExerciseRepositoryInterpreter
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.tasks.DoobieTaskRepositoryInterpreter
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.courses.{
  DoobieCourseCategoryRepositoryInterpreter,
  DoobieCourseRepositoryInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.payment.DoobieTransactionRepositoryInterpreter
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.results.{
  DoobieEnrollmentRepositoryInterpreter,
  DoobieExerciseResultRepositoryInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.schedule.{
  DoobieRoomRepositoryInterpreter,
  DoobieScheduleRepositoryInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.users.{
  DoobieStudentProfileRepositoryInterpreter,
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
      studentRepo = DoobieStudentProfileRepositoryInterpreter[F](xa)
      transactionRepo = DoobieTransactionRepositoryInterpreter[F](xa)
      exerciseResultRepo = DoobieExerciseResultRepositoryInterpreter[F](xa)
      scheduleRepo = DoobieScheduleRepositoryInterpreter[F](xa)
      roomRepo = DoobieRoomRepositoryInterpreter[F](xa)
      enrollmentRepo = DoobieEnrollmentRepositoryInterpreter[F](xa)
      courseCategoryRepo = DoobieCourseCategoryRepositoryInterpreter[F](xa)
      courseRepo = DoobieCourseRepositoryInterpreter[F](xa)
      lessonRepo = DoobieLessonRepositoryInterpreter[F](xa)
      homeworkRepo = DoobieHomeworkRepositoryInterpreter[F](xa)
      exerciseRepo = DoobieExerciseRepositoryInterpreter[F](xa)
      taskRepo = DoobieTaskRepositoryInterpreter[F](xa)

      // Validations init
      userValidation = UserValidationInterpreter[F](userRepo)
      teacherValidation = TeacherProfileValidationInterpreter[F](teacherRepo)
      studentValidation = StudentProfileValidationInterpreter[F](studentRepo)

      // Services init
      userService = UserService[F](userRepo, userValidation)
      teacherService = TeacherProfileService[F](teacherRepo, teacherValidation)
      studentService = StudentProfileService[F](studentRepo, studentValidation)
      transactionService = TransactionService[F](transactionRepo)
      exerciseResultService = ExerciseResultService[F](exerciseResultRepo)
      scheduleService = ScheduleService[F](scheduleRepo)
      roomService = RoomService[F](roomRepo)
      enrollmentService = EnrollmentService[F](enrollmentRepo)
      courseCategoryService = CourseCategoryService[F](courseCategoryRepo)
      courseService = CourseService[F](courseRepo)
      lessonService = LessonService[F](lessonRepo)
      homeworkService = HomeworkService[F](homeworkRepo)
      exerciseService = ExerciseService[F](exerciseRepo)
      taskService = TaskService[F](taskRepo)

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
      roomController = RoomController[F](roomService)

      httpApp = Router(
        "/user" -> UserEndpoints
          .endpoints[F, BCrypt, HMACSHA256](
            routeAuth,
            userController,
            roomController,
            teacherService,
            studentService
          ),
        "/teacher" -> TeacherProfileEndpoints
          .endpoints[F, HMACSHA256](
            teacherService,
            roomController,
            routeAuth
          ),
        "/student" -> StudentProfileEndpoints
          .endpoints[F, HMACSHA256](studentService, roomController, routeAuth),
        "/payment" -> PaymentEndpoints
          .endpoints[F, HMACSHA256](
            transactionService,
            studentService,
            routeAuth
          ),
        "/result" -> ResultEndpoints
          .endpoints[F, HMACSHA256](exerciseResultService, routeAuth),
        "/schedule" -> ScheduleEndpoints
          .endpoints[F, HMACSHA256](scheduleService, routeAuth),
        "/enroll" -> EnrollmentEndpoints
          .endpoints[F, HMACSHA256](enrollmentService, routeAuth),
        "/course_category" -> CourseCategoryEndpoints
          .endpoints[F, HMACSHA256](courseCategoryService, routeAuth),
        "/course" -> CourseEndpoints
          .endpoints[F, HMACSHA256](courseService, routeAuth),
        "/lesson" -> LessonEndpoints
          .endpoints[F, HMACSHA256](lessonService, routeAuth),
        "/homework" -> HomeworkEndpoints
          .endpoints[F, HMACSHA256](homeworkService, routeAuth),
        "/exercise" -> ExerciseEndpoints
          .endpoints[F, HMACSHA256](exerciseService, routeAuth),
        "/task" -> TaskEndpoints
          .endpoints[F, HMACSHA256](taskService, routeAuth)
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
