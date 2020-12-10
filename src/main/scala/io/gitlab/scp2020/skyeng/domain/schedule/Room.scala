package io.gitlab.scp2020.skyeng.domain.schedule

case class Room(
    id: Option[Long] = None,
    studentId: Long,
    teacherId: Option[Long] = None,
    url: String,
    isOpen: Boolean = false
)
