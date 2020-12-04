package io.gitlab.scp2020.skyeng.domain.users.teacher



final case class TeacherRegisterRequest(
                                       userId: Long,
                                       bio: String,
                                       greeting: String,
                                       qualificationType: QualificationType,
                                     ) {
//  def asTeacher[A](user: User): TeacherProfile = {
//    TeacherProfile(
//      userId = user.id,
//      bio,
//      greeting,
//      qualificationType,
//    )
//  }
}
