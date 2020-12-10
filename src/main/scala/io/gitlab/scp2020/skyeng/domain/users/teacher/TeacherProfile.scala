package io.gitlab.scp2020.skyeng.domain.users.teacher

case class TeacherProfile(
    userId: Long,
    bio: String = "",
    greeting: String = "",
    qualification: QualificationType = QualificationType.NotNativeSpeaker
)
