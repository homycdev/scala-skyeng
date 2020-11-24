package io.gitlab.scp2020.skyeng.infrastructure.repository.doobie

import java.time.Instant

import cats._
import cats.data._
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import tsec.authentication.{AugmentedJWT, BackingStore}
import tsec.common.SecureRandomId
import tsec.jws.JWSSerializer
import tsec.jws.mac.{JWSMacCV, JWSMacHeader, JWTMacImpure}
import tsec.mac.jca.{MacErrorM, MacSigningKey}

private object AuthSQL {
  implicit val secureRandomIdPut: Put[SecureRandomId] =
    Put[String].contramap((_: Id[SecureRandomId]).widen)

  def insert[A](jwt: AugmentedJWT[A, Long])(implicit hs: JWSSerializer[JWSMacHeader[A]]): Update0 =
    sql"""INSERT INTO JWT (ID, JWT, IDENTITY)
          VALUES (${jwt.id}, ${jwt.jwt.toEncodedString}, ${jwt.identity}
       """.update

  def update[A](jwt: AugmentedJWT[A, Long])(implicit hs: JWSSerializer[JWSMacHeader[A]]): Update0 =
    sql"""UPDATE JWT SET JWT = ${jwt.jwt.toEncodedString}, IDENTITY = ${jwt.identity}
         | WHERE ID = ${jwt.id}
       """.stripMargin.update

  def delete(id: SecureRandomId): Update0 =
    sql"DELETE FROM JWT WHERE ID = $id".update

  def select(id: SecureRandomId): Query0[(String, Long)] =
    sql"SELECT JWT, IDENTITY FROM JWT WHERE ID = $id"
      .query[(String, Long)]
}

class DoobieAuthRepositoryInterpreter[F[_] : Bracket[*[_], Throwable], A](
                                                                           val key: MacSigningKey[A],
                                                                           val xa: Transactor[F],
                                                                         )(implicit
                                                                           hs: JWSSerializer[JWSMacHeader[A]],
                                                                           s: JWSMacCV[MacErrorM, A],
                                                                         ) extends BackingStore[F, SecureRandomId, AugmentedJWT[A, Long]] {
  override def put(jwt: AugmentedJWT[A, Long]): F[AugmentedJWT[A, Long]] =
    AuthSQL
      .insert(jwt)
      .run
      .transact(xa)
      .as(jwt)

  override def update(jwt: AugmentedJWT[A, Long]): F[AugmentedJWT[A, Long]] =
    AuthSQL
      .update(jwt)
      .run
      .transact(xa)
      .as(jwt)

  override def delete(id: SecureRandomId): F[Unit] =
    AuthSQL
      .delete(id)
      .run
      .transact(xa)
      .void

  override def get(id: SecureRandomId): OptionT[F, AugmentedJWT[A, Long]] =
    OptionT(AuthSQL
      .select(id)
      .option
      .transact(xa))
      .semiflatMap {
        case (jwtStringify, identity) =>
          JWTMacImpure.verifyAndParse(jwtStringify, key) match {
            case Left(err) => err.raiseError[F, AugmentedJWT[A, Long]]
            case Right(jwt) => AugmentedJWT(id, jwt, identity, Instant.now(), Some(Instant.now())).pure[F] // TODO: remove temporary Instant.now()
          }
      }
}


object DoobieAuthRepositoryInterpreter {
  def apply[F[_] : Bracket[*[_], Throwable], A](key: MacSigningKey[A], xa: Transactor[F])(implicit
                                                                                          hs: JWSSerializer[JWSMacHeader[A]],
                                                                                          s: JWSMacCV[MacErrorM, A],
  ): DoobieAuthRepositoryInterpreter[F, A] =
    new DoobieAuthRepositoryInterpreter(key, xa)
}
