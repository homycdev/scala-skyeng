package io.gitlab.scp2020.skyeng.domain.courses.classes

import io.gitlab.scp2020.skyeng.domain.courses.LevelType

case class Class(
                  id: Long,
                  courseId: Long,
                  classType: ClassType,
                  lessonID: Long,
                  difficulty: LevelType,
                  listPosition: Int,
                )
