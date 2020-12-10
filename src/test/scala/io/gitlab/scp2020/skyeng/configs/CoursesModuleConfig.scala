package io.gitlab.scp2020.skyeng.configs

import cats.effect.IO
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
import io.gitlab.scp2020.skyeng.infrastructure.endpoint.AuthTest
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
import io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.classes.{
  HomeworkRepositoryInMemoryInterpreter,
  LessonRepositoryInMemoryInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.exercises.ExerciseRepositoryInMemoryInterpreter
import io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.tasks.TaskRepositoryInMemoryInterpreter
import io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.courses.{
  CourseCategoryRepositoryInMemoryInterpreter,
  CourseRepositoryInMemoryInterpreter
}
import io.gitlab.scp2020.skyeng.infrastructure.repository.inmemory.results.EnrollmentRepositoryInMemoryInterpreter
import org.http4s.HttpRoutes
import tsec.authentication.SecuredRequestHandler

trait CoursesModuleConfig extends UsersModuleConfig {
  val userAuth = new AuthTest[IO](userRepo)

  val courseCategoryRepo: CourseCategoryRepositoryInMemoryInterpreter[IO] =
    CourseCategoryRepositoryInMemoryInterpreter[IO]()
  val courseCategoryService: CourseCategoryService[IO] =
    CourseCategoryService[IO](courseCategoryRepo)

  val courseRepo: CourseRepositoryInMemoryInterpreter[IO] =
    CourseRepositoryInMemoryInterpreter[IO]()
  val courseService: CourseService[IO] = CourseService[IO](courseRepo)

  val enrollmentRepo: EnrollmentRepositoryInMemoryInterpreter[IO] =
    EnrollmentRepositoryInMemoryInterpreter[IO]()
  val enrollmentService: EnrollmentService[IO] =
    EnrollmentService[IO](enrollmentRepo)

  val homeworkRepo: HomeworkRepositoryInMemoryInterpreter[IO] =
    HomeworkRepositoryInMemoryInterpreter[IO]()
  val homeworkService: HomeworkService[IO] = HomeworkService[IO](homeworkRepo)

  val lessonRepo: LessonRepositoryInMemoryInterpreter[IO] =
    LessonRepositoryInMemoryInterpreter[IO]
  val lessonService: LessonService[IO] = LessonService[IO](lessonRepo)

  val taskRepo: TaskRepositoryInMemoryInterpreter[IO] =
    TaskRepositoryInMemoryInterpreter[IO]()
  val taskService: TaskService[IO] = TaskService[IO](taskRepo)

  val exerciseRepo: ExerciseRepositoryInMemoryInterpreter[IO] =
    ExerciseRepositoryInMemoryInterpreter[IO]()
  val exerciseService: ExerciseService[IO] = ExerciseService[IO](exerciseRepo)

  val courseCategoryEndpoints: HttpRoutes[IO] = CourseCategoryEndpoints
    .endpoints(courseCategoryService, SecuredRequestHandler(jwtAuth))
  val courseEndpoints: HttpRoutes[IO] = CourseEndpoints
    .endpoints(courseService, SecuredRequestHandler(jwtAuth))
  val enrollmentEndpoints: HttpRoutes[IO] = EnrollmentEndpoints
    .endpoints(enrollmentService, SecuredRequestHandler(jwtAuth))
  val homeworkEndpoints: HttpRoutes[IO] = HomeworkEndpoints
    .endpoints(homeworkService, SecuredRequestHandler(jwtAuth))
  val lessonEndpoints: HttpRoutes[IO] = LessonEndpoints
    .endpoints(lessonService, SecuredRequestHandler(jwtAuth))
  val taskEndpoints: HttpRoutes[IO] = TaskEndpoints
    .endpoints(taskService, SecuredRequestHandler(jwtAuth))
  val exerciseEndpoints: HttpRoutes[IO] = ExerciseEndpoints
    .endpoints(exerciseService, SecuredRequestHandler(jwtAuth))
}
object CoursesModuleConfig extends CoursesModuleConfig
