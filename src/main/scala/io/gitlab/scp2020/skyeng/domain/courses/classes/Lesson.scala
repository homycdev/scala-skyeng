package io.gitlab.scp2020.skyeng.domain.courses.classes

case class Lesson(
    id: Option[Long],
    title: String,
    courseId: Option[Long],
    difficulty: LevelType,
    listPosition: Int
)
