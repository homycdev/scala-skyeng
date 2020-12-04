package io.gitlab.scp2020.skyeng.domain.schedule

import java.time.LocalDateTime

case class Schedule(
                     id: Option[Long],
                     studentId: Long,
                     teacherId: Long,
                     startTime: LocalDateTime,
                     durationSeconds: Int,
                   )
