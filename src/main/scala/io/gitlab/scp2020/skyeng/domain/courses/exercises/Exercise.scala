package io.gitlab.scp2020.skyeng.domain.courses.exercises

import io.circe.Json

case class Exercise(
    id: Option[Long] = None,
    taskId: Option[Long] = None,
    exerciseType: ExerciseType,
    content: Json
)
