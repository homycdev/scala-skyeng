package io.gitlab.scp2020.skyeng.domain.courses

case class Enrollment(
    id: Option[Long] = None,
    studentId: Long,
    courseId: Long
)
