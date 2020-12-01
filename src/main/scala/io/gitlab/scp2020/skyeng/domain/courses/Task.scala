package io.gitlab.scp2020.skyeng.domain.courses

case class Task(
                 id: Long,
                 classId: Option[Long],
                 taskType: TaskType,
                 listPosition: Int,
               )
