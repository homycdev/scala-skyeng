package io.gitlab.scp2020.skyeng.domain

import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  Class,
  ClassVocabulary,
  Homework,
  Lesson
}
import io.gitlab.scp2020.skyeng.domain.courses.exercises.Exercise
import io.gitlab.scp2020.skyeng.domain.courses.tasks.Task
import io.gitlab.scp2020.skyeng.domain.courses.vocabulary.Word
import io.gitlab.scp2020.skyeng.domain.courses.{Course, CourseCategory}
import io.gitlab.scp2020.skyeng.domain.payment.Transaction
import io.gitlab.scp2020.skyeng.domain.users.User
import io.gitlab.scp2020.skyeng.domain.users.student.StudentProfile
import io.gitlab.scp2020.skyeng.domain.users.teacher.TeacherProfile

// ENUM class
sealed trait ValidationError extends Product with Serializable

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

case class CourseCategoryAlreadyExistsError(category: CourseCategory)
    extends ValidationError

case object CourseCategoryNotFoundError extends ValidationError

case class CourseAlreadyExistsError(course: Course) extends ValidationError

case object CourseNotFoundError extends ValidationError

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

case class TransactionAlreadyExistsError(transaction: Transaction)
    extends ValidationError

case object TransactionNotFoundError extends ValidationError
