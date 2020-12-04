package io.gitlab.scp2020.skyeng.domain.results

case class TaskResult(
                       id: Option[Long],
                       studentId: Long,
                       taskId: Long,
                       score: Int
                     )
