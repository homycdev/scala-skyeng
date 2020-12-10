package io.gitlab.scp2020.skyeng.domain.users.student

case class StudentProfile(
    userId: Long,
    teacherId: Option[Long] = None,
    balance: Int = 0
)
