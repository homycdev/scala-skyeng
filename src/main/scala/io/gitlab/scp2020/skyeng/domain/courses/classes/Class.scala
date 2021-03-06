package io.gitlab.scp2020.skyeng.domain.courses.classes

case class Class(
    id: Option[Long],
    title: String,
    courseId: Option[Long],
    classType: ClassType,
    lessonId: Option[Long],
    difficulty: LevelType,
    listPosition: Int
)
