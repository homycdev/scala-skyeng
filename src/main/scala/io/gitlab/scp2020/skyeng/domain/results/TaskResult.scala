package io.gitlab.scp2020.skyeng.domain.results

case class TaskResult(
                       id: Long,
                       studentId: Long,
                       taskId: Long,
                       score: Int
                     )
