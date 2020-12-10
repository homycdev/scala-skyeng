package io.gitlab.scp2020.skyeng.domain.courses.classes

case class Lesson(
    id: Option[Long] = None,
    title: String,
    courseId: Option[Long] = None,
    difficulty: LevelType,
    listPosition: Int
)
