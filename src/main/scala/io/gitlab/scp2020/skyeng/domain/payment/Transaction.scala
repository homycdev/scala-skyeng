package io.gitlab.scp2020.skyeng.domain.payment

import java.time.LocalDateTime

case class Transaction(
    id: Option[Long],
    studentId: Long,
    teacherId: Option[Long],
    created: LocalDateTime,
    status: TransactionStatus,
    change: Int,
    reminder: Int
)
