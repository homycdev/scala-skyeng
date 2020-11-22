package io.gitlab.scp2020.skyeng.domain

import io.gitlab.scp2020.skyeng.domain.users.User


// ENUM class
sealed trait ValidationError extends Product with Serializable
case object UserAlreadyExists extends ValidationError
case class UserAlreadyExistsError(user: User) extends ValidationError
case class UserAuthenticationFailedError(userName: String) extends ValidationError
case object UserNotFoundError extends ValidationError