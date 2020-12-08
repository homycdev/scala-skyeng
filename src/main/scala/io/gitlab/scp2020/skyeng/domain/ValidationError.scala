package io.gitlab.scp2020.skyeng.domain

import io.gitlab.scp2020.skyeng.domain.courses.classes.{Class, ClassVocabulary, Homework, Lesson}
import io.gitlab.scp2020.skyeng.domain.courses.exercises.Exercise
import io.gitlab.scp2020.skyeng.domain.courses.tasks.Task
import io.gitlab.scp2020.skyeng.domain.courses.vocabulary.Word
import io.gitlab.scp2020.skyeng.domain.courses.{Course, CourseCategory, Enrollment}
import io.gitlab.scp2020.skyeng.domain.results.{ClassResult, ExerciseResult, TaskResult}
import io.gitlab.scp2020.skyeng.domain.payment.Transaction
import io.gitlab.scp2020.skyeng.domain.schedule.{Room, Schedule}
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.users.student.StudentProfile
import io.gitlab.scp2020.skyeng.domain.users.teacher.TeacherProfile

// ENUM class
sealed trait ValidationError extends Product with Serializable

// users module
case object UserAlreadyExists extends ValidationError

case class UserAlreadyExistsError(user: User) extends ValidationError

case class UserAuthenticationFailedError(userName: String)
    extends ValidationError

case object UserNotFoundError extends ValidationError

case class TeacherAlreadyExistsError(teacher: TeacherProfile)
    extends ValidationError

case object TeacherNotFoundError extends ValidationError

case class StudentAlreadyExistsError(student: StudentProfile)
    extends ValidationError

case object StudentNotFoundError extends ValidationError

// courses module
case class CourseCategoryAlreadyExistsError(category: CourseCategory)
    extends ValidationError

case object CourseCategoryNotFoundError extends ValidationError

case class CourseAlreadyExistsError(course: Course) extends ValidationError

case object CourseNotFoundError extends ValidationError

case class EnrollmentAlreadyExistsError(enrollement: Enrollment) extends ValidationError

case object EnrollmentNotFoundError extends ValidationError

case class WordAlreadyExistsError(word: Word) extends ValidationError

case object WordNotFoundError extends ValidationError

case class TaskAlreadyExistsError(task: Task) extends ValidationError

case object TaskNotFoundError extends ValidationError

case class ExerciseAlreadyExistsError(exercise: Exercise)
    extends ValidationError

case object ExerciseNotFoundError extends ValidationError

case class ClassVocabularyAlreadyExistsError(vocabulary: ClassVocabulary)
    extends ValidationError

case object ClassVocabularyNotFoundError extends ValidationError

case class ClassObjectAlreadyExistsError(classObj: Class)
    extends ValidationError

case object ClassObjectNotFoundError extends ValidationError

case class LessonAlreadyExistsError(lesson: Lesson) extends ValidationError

case object LessonNotFoundError extends ValidationError

case class HomeworkAlreadyExistsError(homework: Homework)
    extends ValidationError

case object HomeworkNotFoundError extends ValidationError

// results module
case class ClassResultAlreadyExistsError(result: ClassResult)
    extends ValidationError

case object ClassResultNotFoundError extends ValidationError

case class TaskResultAlreadyExistsError(result: TaskResult)
    extends ValidationError

case object TaskResultNotFoundError extends ValidationError

case class ExerciseResultAlreadyExistsError(result: ExerciseResult)
    extends ValidationError

case object ExerciseResultNotFoundError extends ValidationError

// payment module
case class TransactionAlreadyExistsError(transaction: Transaction)
    extends ValidationError

case object TransactionNotFoundError extends ValidationError

// schedule module
case class RoomAlreadyExistsError(room: Room) extends ValidationError

case object RoomNotFoundError extends ValidationError

case class ScheduleAlreadyExistsError(schedule: Schedule)
    extends ValidationError

case object ScheduleNotFoundError extends ValidationError
