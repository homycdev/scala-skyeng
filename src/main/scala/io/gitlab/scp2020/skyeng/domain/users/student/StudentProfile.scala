package io.gitlab.scp2020.skyeng.domain.users.student

case class StudentProfile(
                           userId: Long,
                           teacherId: Long,
                           balance: Int
                         )
