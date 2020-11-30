package io.gitlab.scp2020.skyeng.domain.courses

case class Task(
                 id: Long,
                 classId: Long,
                 taskType: TaskType,
                 listPosition: Int,
               )
