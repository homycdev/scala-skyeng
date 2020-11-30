package io.gitlab.scp2020.skyeng.domain.courses

import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed trait LevelType extends EnumEntry with Snakecase

case object LevelType extends Enum[LevelType]
  with CirceEnum[LevelType] {

  case object Begginer extends LevelType

  case object Elementary extends LevelType

  case object PreIntermediate extends LevelType

  case object Intermediate extends LevelType

  case object UpperIntermediate extends LevelType

  case object Advanced extends LevelType

  val values = findValues
}
