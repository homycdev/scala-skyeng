package io.gitlab.scp2020.skyeng.domain.results

import io.circe.Json

case class ExerciseResult(
                           id: Option[Long],
                           studentId: Long,
                           exerciseId: Long,
                           score: Int,
                           content: Json
                         )
