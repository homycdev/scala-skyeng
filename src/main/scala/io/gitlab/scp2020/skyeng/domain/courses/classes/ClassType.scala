package io.gitlab.scp2020.skyeng.domain.courses.classes

import enumeratum.EnumEntry.Snakecase
import enumeratum._

import scala.collection.immutable

sealed trait ClassType extends EnumEntry with Snakecase

case object ClassType extends Enum[ClassType] with CirceEnum[ClassType] {

  case object Lesson extends ClassType

  case object Homework extends ClassType

  case object Other extends ClassType

  val values: immutable.IndexedSeq[ClassType] = findValues
}
