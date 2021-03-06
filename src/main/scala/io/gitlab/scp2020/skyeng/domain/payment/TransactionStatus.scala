package io.gitlab.scp2020.skyeng.domain.payment

import enumeratum.EnumEntry.Snakecase
import enumeratum._

import scala.collection.immutable

sealed trait TransactionStatus extends EnumEntry with Snakecase

case object TransactionStatus
    extends Enum[TransactionStatus]
    with CirceEnum[TransactionStatus] {

  case object Replenishment extends TransactionStatus

  case object LessonCompleted extends TransactionStatus

  val values: immutable.IndexedSeq[TransactionStatus] = findValues
}
