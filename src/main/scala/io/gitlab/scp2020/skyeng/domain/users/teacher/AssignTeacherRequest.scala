package io.gitlab.scp2020.skyeng.domain.users.teacher

import io.gitlab.scp2020.skyeng.domain.users.User


final case class AssignTeacherRequest(
                                       userId: Long,
                                       bio: String,
                                       greeting: String,
                                       qualificationType: QualificationType,
                                     ) {
  def asTeacher[A](user: User): TeacherProfile = {
    TeacherProfile(
      userId = user.id.get,
      bio,
      greeting,
      qualificationType,
    )
  }
}
