package io.gitlab.scp2020.skyeng.domain.schedule

case class Room(
                 id: Long,
                 studentId: Long,
                 teacherId: Option[Long],
                 url: String,
                 isOpen: Boolean
               )
