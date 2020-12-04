package io.gitlab.scp2020.skyeng.infrastructure.repository.helpers

import doobie.{Get, Put}
import io.circe.Json
import io.gitlab.scp2020.skyeng.domain.courses.classes.ClassType.{
  Homework,
  Lesson
}
import io.gitlab.scp2020.skyeng.domain.courses.classes.LevelType.{
  Advanced,
  Beginner,
  Elementary,
  Intermediate,
  PreIntermediate,
  UpperIntermediate
}
import io.gitlab.scp2020.skyeng.domain.courses.classes.{ClassType, LevelType}
import io.gitlab.scp2020.skyeng.domain.courses.exercises.ExerciseType
import io.gitlab.scp2020.skyeng.domain.courses.exercises.ExerciseType.{
  FillBlanks,
  Match,
  TrueFalse
}
import io.gitlab.scp2020.skyeng.domain.courses.tasks.TaskType
import io.gitlab.scp2020.skyeng.domain.courses.tasks.TaskType.{
  Grammar,
  Listening,
  Reading,
  Vocabulary,
  Writing
}
import io.gitlab.scp2020.skyeng.domain.users.teacher.QualificationType
import io.gitlab.scp2020.skyeng.domain.users.teacher.QualificationType.{
  NativeSpeaker,
  NotNativeSpeaker
}

object DoobieCustomMapping {
  def toClassType(c: ClassType): String =
    c match {
      case Lesson          => "lesson"
      case Homework        => "homework"
      case ClassType.Other => "other"
    }

  def fromClassType(s: String): ClassType =
    s match {
      case "lesson"   => Lesson
      case "homework" => Homework
      case _          => ClassType.Other
    }

  def toTaskType(c: TaskType): String =
    c match {
      case Vocabulary     => "vocabulary"
      case Grammar        => "grammar"
      case Writing        => "writing"
      case Reading        => "reading"
      case Listening      => "listening"
      case TaskType.Other => "other"
    }

  def fromTaskType(s: String): TaskType =
    s match {
      case "vocabulary" => Vocabulary
      case "grammar"    => Grammar
      case "writing"    => Writing
      case "reading"    => Reading
      case "listening"  => Listening
      case _            => TaskType.Other
    }

  def toQualificationType(c: QualificationType): String =
    c match {
      case NotNativeSpeaker => "not_native_speaker"
      case NativeSpeaker    => "native_speaker"
    }

  def fromQualificationType(s: String): QualificationType =
    s match {
      case "not_native_speaker" => NotNativeSpeaker
      case "native_speaker"     => NativeSpeaker
    }

  def toLevelType(c: LevelType): String =
    c match {
      case Beginner          => "beginner"
      case Elementary        => "elementary"
      case PreIntermediate   => "pre_intermediate"
      case Intermediate      => "intermediate"
      case UpperIntermediate => "upper_intermediate"
      case Advanced          => "advanced"
    }

  def fromLevelType(s: String): LevelType =
    s match {
      case "elementary"         => Elementary
      case "pre_intermediate"   => PreIntermediate
      case "intermediate"       => Intermediate
      case "upper_intermediate" => UpperIntermediate
      case "advanced"           => Advanced
      case _                    => Beginner
    }

  def toExerciseType(c: ExerciseType): String =
    c match {
      case TrueFalse          => "true_false"
      case FillBlanks         => "fill_blanks"
      case Match              => "match"
      case ExerciseType.Other => "other"
    }

  def fromExerciseType(s: String): ExerciseType =
    s match {
      case "true_false"  => TrueFalse
      case "fill_blanks" => FillBlanks
      case "match"       => Match
      case _             => ExerciseType.Other
    }

  object implicits {
    import doobie.postgres.circe.json.implicits._

    implicit val jsonObjectGet: Get[Json] = jsonGet
    implicit val jsonObjectPut: Put[Json] = jsonPut

    implicit val classTypeGet: Get[ClassType] =
      Get[String].tmap(fromClassType)
    implicit val classTypePut: Put[ClassType] =
      Put[String].tcontramap(toClassType)

    implicit val levelTypeGet: Get[LevelType] =
      Get[String].tmap(fromLevelType)
    implicit val levelTypePut: Put[LevelType] =
      Put[String].tcontramap(toLevelType)

    implicit val exerciseTypeGet: Get[ExerciseType] =
      Get[String].tmap(fromExerciseType)
    implicit val exerciseTypePut: Put[ExerciseType] =
      Put[String].tcontramap(toExerciseType)

    implicit val taskTypeGet: Get[TaskType] =
      Get[String].tmap(fromTaskType)
    implicit val taskTypePut: Put[TaskType] =
      Put[String].tcontramap(toTaskType)
  }
}
