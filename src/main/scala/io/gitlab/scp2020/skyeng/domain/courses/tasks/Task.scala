package io.gitlab.scp2020.skyeng.domain.courses.tasks

case class Task(
    id: Option[Long] = None,
    classId: Option[Long] = None,
    taskType: TaskType,
    listPosition: Int
)
