package io.gitlab.scp2020.skyeng.infrastructure.repository.helpers

import io.gitlab.scp2020.skyeng.domain.courses.ExerciseType.{FillBlanks, Match, TrueFalse}
import io.gitlab.scp2020.skyeng.domain.courses.LevelType.{Advanced, Begginer, Elementary, Intermediate, PreIntermediate, UpperIntermediate}
import io.gitlab.scp2020.skyeng.domain.courses.TaskType.{Grammar, Listening, Reading, Vocabulary, Writing}
import io.gitlab.scp2020.skyeng.domain.courses.classes.ClassType
import io.gitlab.scp2020.skyeng.domain.courses.classes.ClassType.{Homework, Lesson}
import io.gitlab.scp2020.skyeng.domain.courses.{ExerciseType, LevelType, TaskType}
import io.gitlab.scp2020.skyeng.domain.users.teacher.QualificationType
import io.gitlab.scp2020.skyeng.domain.users.teacher.QualificationType.{NativeSpeaker, NotNativeSpeaker}


object DoobieCustomMapping {
  def toClassType(c: ClassType): String =
    c match {
      case Lesson => "lesson"
      case Homework => "homework"
    }

  def fromClassType(s: String): Option[ClassType] =
    Option(s) collect {
      case "lesson" => Lesson
      case "homework" => Homework
    }

  def toTaskType(c: TaskType): String =
    c match {
      case Vocabulary => "vocabulary"
      case Grammar => "grammar"
      case Writing => "writing"
      case Reading => "reading"
      case Listening => "listening"
    }

  def fromTaskType(s: String): Option[TaskType] =
    Option(s) collect {
      case "vocabulary" => Vocabulary
      case "grammar" => Grammar
      case "writing" => Writing
      case "reading" => Reading
      case "listening" => Listening
    }

  def toQualificationType(c: QualificationType): String =
    c match {
      case NotNativeSpeaker => "not_native_speaker"
      case NativeSpeaker => "native_speaker"
    }

  def fromQualificationType(s: String): Option[QualificationType] =
    Option(s) collect {
      case "not_native_speaker" => NotNativeSpeaker
      case "native_speaker" => NativeSpeaker
    }

  def toLevelType(c: LevelType): String =
    c match {
      case Begginer => "begginer"
      case Elementary => "elementary"
      case PreIntermediate => "pre_intermediate"
      case Intermediate => "intermediate"
      case UpperIntermediate => "upper_intermediate"
      case Advanced => "advanced"
    }

  def fromLevelType(s: String): Option[LevelType] =
    Option(s) collect {
      case "begginer" => Begginer
      case "elementary" => Elementary
      case "pre_intermediate" => PreIntermediate
      case "intermediate" => Intermediate
      case "upper_intermediate" => UpperIntermediate
      case "advanced" => Advanced
    }

  def toExerciseType(c: ExerciseType): String =
    c match {
      case TrueFalse => "true_false"
      case FillBlanks => "fill_blanks"
      case Match => "match"
    }

  def fromExerciseType(s: String): Option[ExerciseType] =
    Option(s) collect {
      case "true_false" => TrueFalse
      case "fill_blanks" => FillBlanks
      case "match" => Match
    }
}
