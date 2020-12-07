package io.gitlab.scp2020.skyeng.domain.courses.classes

case class Homework(
    id: Option[Long],
    title: String,
    courseId: Option[Long],
    lessonId: Option[Long],
    difficulty: LevelType,
    listPosition: Int
)
