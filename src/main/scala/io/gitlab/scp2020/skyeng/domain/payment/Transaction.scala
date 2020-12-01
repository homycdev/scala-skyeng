package io.gitlab.scp2020.skyeng.domain.payment

case class Transaction(
                        id: Long,
                        studentId: Long,
                        teacherId: Option[Long],
                        status: TransactionStatus,
                        change: Int,
                        reminder: Int,  // TODO: add 'created' field
                      )
