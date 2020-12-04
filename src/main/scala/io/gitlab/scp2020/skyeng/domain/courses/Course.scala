package io.gitlab.scp2020.skyeng.domain.courses

case class Course(
                   id: Option[Long],
                   title: String,
                   categoryId: Option[Long],
                 )
