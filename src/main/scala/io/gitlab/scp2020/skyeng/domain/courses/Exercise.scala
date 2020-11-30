package io.gitlab.scp2020.skyeng.domain.courses

import io.circe.Json


case class Exercise(
                     id: Long,
                     taskId: Long,
                     exerciseType: ExerciseType,
                     content: Json
                   )
