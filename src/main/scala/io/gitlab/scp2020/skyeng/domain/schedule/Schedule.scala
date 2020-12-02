package io.gitlab.scp2020.skyeng.domain.schedule

case class Schedule(
                     id: Long,
                     studentId: Long,
                     teacherId: Long, // TODO: add 'start_time' field
                     durationSeconds: Int,
                   )
