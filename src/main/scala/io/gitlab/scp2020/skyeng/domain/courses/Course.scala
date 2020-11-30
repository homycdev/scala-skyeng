package io.gitlab.scp2020.skyeng.domain.courses

case class Course(
                   id: Long,
                   title: String,
                   categoryId: CourseCategory,
                 )
