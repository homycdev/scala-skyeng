package io.gitlab.scp2020.skyeng.domain

import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.users.student.StudentProfile
import io.gitlab.scp2020.skyeng.domain.users.teacher.TeacherProfile


// ENUM class
sealed trait ValidationError extends Product with Serializable

case object UserAlreadyExists extends ValidationError

case class UserAlreadyExistsError(user: User) extends ValidationError

case class UserAuthenticationFailedError(userName: String) extends ValidationError

case object UserNotFoundError extends ValidationError

case class TeacherAlreadyExistsError(teacher: TeacherProfile) extends ValidationError

case object TeacherNotFoundError extends ValidationError

case class StudentAlreadyExistsError(student: StudentProfile) extends ValidationError

case object StudentNotFoundError extends ValidationError