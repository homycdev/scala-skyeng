package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.users

import java.time.LocalDateTime

import cats.data.OptionT
import cats.effect.Bracket
import cats.implicits.catsSyntaxOptionId
import cats.syntax.all._
import doobie.implicits.{toSqlInterpolator, _}
import doobie.{Query0, Transactor, Update0}
import io.gitlab.scp2020.skyeng.domain.users.{User, UserRepositoryAlgebra}
import io.gitlab.scp2020.skyeng.infrastructure.repository.doobie.SQLPagination.paginate
import io.gitlab.scp2020.skyeng.infrastructure.repository.helpers.DoobieCustomMapping.implicits._
import tsec.authentication.IdentityStore

private object UserSQL {
  def insert(user: User): Update0 =
    sql"""
    INSERT INTO "user" (user_name, first_name, last_name, birth_date, gender,
    email, hash, phone_number, role, created)
    VALUES (${user.userName}, ${user.firstName}, ${user.lastName}, ${user.birthDate},
    ${user.gender}, ${user.email}, ${user.hash}, ${user.phone}, ${user.role}, ${user.created})
  """.update

  def update(user: User, id: Long): Update0 =
    sql"""
    UPDATE "user"
    SET first_name = ${user.firstName}, last_name = ${user.lastName}, 
    birth_date = ${user.birthDate}, gender = ${user.gender}, 
    email = ${user.email}, hash = ${user.hash}, phone_number = ${user.phone}, role = ${user.role}
    WHERE id = $id
  """.update

  def select(userId: Long): Query0[User] =
    sql"""
    SELECT user_name, first_name, last_name, birth_date, gender, email, hash, phone_number, role, created, id
    FROM "user"
    WHERE id = $userId
  """.query[User]

  def byUserName(userName: String): Query0[User] = sql"""
    SELECT user_name, first_name, last_name, birth_date, gender, email, hash, phone_number, role, created, id
    FROM "user"
    WHERE user_name = $userName
  """.query[User]

  def delete(userId: Long): Update0 =
    sql"""
    DELETE FROM "user" WHERE id = $userId
  """.update

  def selectAll: Query0[User] =
    sql"""
    SELECT user_name, first_name, last_name, birth_date, gender, email, hash, phone_number, role, created, id
    FROM "user"
  """.query[User]
}

class DoobieUserRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends UserRepositoryAlgebra[F]
    with IdentityStore[F, Long, User] {
  self =>

  import UserSQL._

  def create(user: User): F[User] =
    insert(user)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => user.copy(id = id.some, created = LocalDateTime.now()))
      .transact(xa)

  def update(user: User): OptionT[F, User] =
    OptionT.fromOption[F](user.id).semiflatMap { id =>
      UserSQL.update(user, id).run.transact(xa).as(user)
    }

  def get(userId: Long): OptionT[F, User] =
    OptionT(select(userId).option.transact(xa))

  def findByUserName(userName: String): OptionT[F, User] =
    OptionT(byUserName(userName).option.transact(xa))

  def delete(userId: Long): OptionT[F, User] =
    get(userId).semiflatMap(user =>
      UserSQL.delete(userId).run.transact(xa).as(user)
    )

  def deleteByUserName(userName: String): OptionT[F, User] =
    findByUserName(userName).mapFilter(_.id).flatMap(delete)

  def list(pageSize: Int, offset: Int): F[List[User]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}

object DoobieUserRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
      xa: Transactor[F]
  ): DoobieUserRepositoryInterpreter[F] =
    new DoobieUserRepositoryInterpreter(xa)
}
