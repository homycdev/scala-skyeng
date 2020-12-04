package io.gitlab.scp2020.skyeng.domain.courses.tasks

case class Task(
    id: Option[Long],
    classId: Option[Long],
    taskType: TaskType,
    listPosition: Int
)
