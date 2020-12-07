package io.gitlab.scp2020.skyeng.domain.authentication

import java.time.LocalDateTime

import io.gitlab.scp2020.skyeng.domain.users.{Role, User}
import tsec.passwordhashers.PasswordHash

final case class LoginRequest(
    userName: String,
    password: String
)

final case class SignupRequest(
    userName: String,
    firstName: Option[String],
    lastName: Option[String],
    birthDate: Option[String],
    gender: Option[String],
    email: String,
    password: String,
    phone: Option[String] = None,
    role: Role = Role("Student"),
) {
  def asUser[A](hashedPassword: PasswordHash[A]): User =
    User(
      userName = userName,
      firstName = firstName,
      lastName = lastName,
      email = email,
      hash = hashedPassword,
      phone = phone,
      role = role,
      created =  LocalDateTime.now(),
      birthDate = birthDate,
      gender = gender
    )
}
