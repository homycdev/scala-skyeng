package io.gitlab.scp2020.skyeng.config

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import cats.syntax.functor._
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class DatabaseConnectionsConfig(poolSize: Int)


case class DatabaseConfig(
                           url: String,
                           driver: String,
                           user: String,
                           password: String,
                           connections: DatabaseConnectionsConfig,
                         )

object DatabaseConfig {
  def dbTransactor[F[_] : Async : ContextShift](
                                                 dbc: DatabaseConfig,
                                                 connEc: ExecutionContext,
                                                 blocker: Blocker,
                                               ): Resource[F, HikariTransactor[F]] =
    HikariTransactor
      .newHikariTransactor[F](
        dbc.driver,
        dbc.url,
        dbc.user,
        dbc.password,
        connEc,
        blocker
      )

  def initDb[F[_]](config: DatabaseConfig)(implicit s: Sync[F]): F[Unit] =
    s.delay {
      val flyWay: Flyway =
        Flyway
          .configure()
          .dataSource(config.url, config.user, config.password)
          .load()
      flyWay.repair()
      flyWay.migrate()
    }.as(())

}