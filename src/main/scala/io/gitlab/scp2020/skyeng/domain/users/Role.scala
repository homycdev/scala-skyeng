package io.gitlab.scp2020.skyeng.domain.users

import cats._
import tsec.authorization.{AuthGroup, SimpleAuthEnum}

final case class Role(roleRepr: String)

object Role extends SimpleAuthEnum[Role, String] {
  val Admin: Role = Role("admin")
  val Student: Role = Role("student")
  val Teacher: Role = Role("teacher")

  override val values: AuthGroup[Role] = AuthGroup(Admin, Student, Teacher)

  override def getRepr(t: Role): String = t.roleRepr

  implicit val eqRole: Eq[Role] = Eq.fromUniversalEquals[Role]
}
