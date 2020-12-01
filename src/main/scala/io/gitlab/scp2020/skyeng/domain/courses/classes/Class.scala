package io.gitlab.scp2020.skyeng.domain.courses.classes

import io.gitlab.scp2020.skyeng.domain.courses.LevelType

case class Class(
                  id: Long,
                  courseId: Option[Long],
                  classType: ClassType,
                  lessonId: Option[Long],
                  difficulty: LevelType,
                  listPosition: Int,
                )
