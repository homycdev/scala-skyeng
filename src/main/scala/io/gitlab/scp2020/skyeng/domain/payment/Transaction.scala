package io.gitlab.scp2020.skyeng.domain.payment

import java.time.LocalDateTime

case class Transaction(
    id: Option[Long] = None,
    studentId: Long,
    teacherId: Option[Long] = None,
    created: LocalDateTime,
    status: TransactionStatus,
    change: Int,
    reminder: Int
)
