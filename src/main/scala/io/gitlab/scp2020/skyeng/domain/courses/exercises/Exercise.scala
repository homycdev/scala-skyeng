package io.gitlab.scp2020.skyeng.domain.courses.exercises

import io.circe.Json

case class Exercise(
                     id: Option[Long],
                     taskId: Option[Long],
                     exerciseType: ExerciseType,
                     content: Json
                   )
