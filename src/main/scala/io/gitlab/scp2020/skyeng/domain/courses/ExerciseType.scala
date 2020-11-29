package io.gitlab.scp2020.skyeng.domain.courses

import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed trait ExerciseType extends EnumEntry with Snakecase

case object ExerciseType extends Enum[ExerciseType]
  with CirceEnum[ExerciseType] {

  case object TrueFalse extends ExerciseType

  case object FillBlanks extends ExerciseType

  case object Match extends ExerciseType

  val values = findValues
}