package io.gitlab.scp2020.skyeng.domain.authentication

import io.circe.Json
import io.gitlab.scp2020.skyeng.domain.courses.Course
import io.gitlab.scp2020.skyeng.domain.courses.classes.{Lesson, LevelType}
import io.gitlab.scp2020.skyeng.domain.schedule.Schedule
import io.gitlab.scp2020.skyeng.domain.users.{Role, User}
import tsec.passwordhashers.PasswordHash

import java.time.LocalDateTime

final case class LoginRequest(
    userName: String,
    password: String
)

final case class SignupRequest(
    userName: String,
    firstName: Option[String],
    lastName: Option[String],
    birthDate: Option[String],
    gender: Option[String],
    email: String,
    password: String,
    phone: Option[String] = None,
    role: Role
) {
  def asUser[A](hashedPassword: PasswordHash[A]): User =
    User(
      userName = userName,
      firstName = firstName,
      lastName = lastName,
      email = email,
      hash = hashedPassword,
      phone = phone,
      role = role,
      created = LocalDateTime.now(),
      birthDate = birthDate,
      gender = gender
    )
}

final case class UpdateRequest(
    userName: Option[String] = None,
    firstName: Option[String] = None,
    lastName: Option[String] = None,
    birthDate: Option[String] = None,
    gender: Option[String] = None,
    email: Option[String] = None,
    phone: Option[String] = None
) {
  def asUser[A](user: User): User = {
    User(
      userName = userName.getOrElse(user.userName),
      firstName = firstName.orElse(user.firstName),
      lastName = lastName.orElse(user.lastName),
      email = email.getOrElse(user.email),
      hash = user.hash,
      phone = phone.orElse(user.phone),
      role = user.role,
      created = user.created,
      birthDate = birthDate,
      gender = gender,
      id = user.id
    )
  }
}

final case class ReplenishRequest(
    amount: Int
)

final case class ExerciseResultRequest(
    content: Json
)

final case class StudentUpdateRequest(
    teacherId: Option[Long]
)

final case class CourseCategoryRequest(
    title: String
)

final case class CourseCreateRequest(
    title: String,
    categoryId: Option[Long] = None
)

final case class CourseUpdateRequest(
    title: Option[String] = None,
    categoryId: Option[Long] = None
) {
  def asCourse[A](course: Course): Course = {
    Course(
      id = course.id,
      title = title.getOrElse(course.title),
      categoryId = categoryId.orElse(course.categoryId)
    )
  }
}

final case class LessonUpdateRequest(
    title: Option[String] = None,
    courseId: Option[Long] = None,
    difficulty: Option[LevelType] = None,
    listPosition: Option[Int] = None
) {
  def asLesson[A](lesson: Lesson): Lesson = {
    Lesson(
      id = lesson.id,
      title = title.getOrElse(lesson.title),
      courseId = courseId.orElse(lesson.courseId),
      difficulty = difficulty.getOrElse(lesson.difficulty),
      listPosition = listPosition.getOrElse(lesson.listPosition)
    )
  }
}

final case class ScheduleUpdateRequest(
    studentId: Option[Long] = None,
    teacherId: Option[Long] = None,
    startTime: Option[LocalDateTime] = None,
    durationSeconds: Option[Int] = None
) {
  def asSchedule[A](schedule: Schedule): Schedule = {
    Schedule(
      id = schedule.id,
      studentId = studentId.getOrElse(schedule.studentId),
      teacherId = teacherId.getOrElse(schedule.teacherId),
      startTime = startTime.getOrElse(schedule.startTime),
      durationSeconds = durationSeconds.getOrElse(schedule.durationSeconds)
    )
  }
}
