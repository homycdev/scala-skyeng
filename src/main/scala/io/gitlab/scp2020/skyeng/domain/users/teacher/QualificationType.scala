package io.gitlab.scp2020.skyeng.domain.users.teacher

import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed trait QualificationType extends EnumEntry with Snakecase

case object QualificationType extends Enum[QualificationType]
  with CirceEnum[QualificationType] {

  case object NotNativeSpeaker extends QualificationType

  case object NativeSpeaker extends QualificationType

  val values = findValues
}
