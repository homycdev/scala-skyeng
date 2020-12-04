package io.gitlab.scp2020.skyeng.domain.courses.tasks

import enumeratum.EnumEntry.Snakecase
import enumeratum._

import scala.collection.immutable

sealed trait TaskType extends EnumEntry with Snakecase

case object TaskType extends Enum[TaskType] with CirceEnum[TaskType] {

  case object Vocabulary extends TaskType

  case object Grammar extends TaskType

  case object Writing extends TaskType

  case object Reading extends TaskType

  case object Listening extends TaskType

  case object Other extends TaskType

  val values: immutable.IndexedSeq[TaskType] = findValues
}
