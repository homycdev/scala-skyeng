package io.gitlab.scp2020.skyeng.domain.courses.classes

case class Homework(
    id: Option[Long] = None,
    title: String,
    courseId: Option[Long] = None,
    lessonId: Option[Long] = None,
    difficulty: LevelType,
    listPosition: Int
)
