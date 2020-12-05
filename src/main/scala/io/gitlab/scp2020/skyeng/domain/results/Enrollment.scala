package io.gitlab.scp2020.skyeng.domain.results

case class Enrollment(
    id: Option[Long],
    studentId: Long,
    courseId: Long
)
