package users.main

import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.LoggerFactory

import com.comcast.ip4s.{Host, Port}

import cats.*
import cats.data.*
import cats.effect.*
import cats.syntax.all.*

import fs2.io.net.Network
import users.config.ApplicationConfig
import users.config.HttpConfig
import users.services.UserManagement

object HttpService:
  def reader[F[_]: Applicative]: ReaderT[F, HttpConfig, HttpService] = ReaderT(HttpService.apply(_).pure)
  def fromApplicationConfig[F[_]: Applicative]: ReaderT[F, ApplicationConfig, HttpService] = reader.local(_.httpConfig)

final case class HttpService(config: HttpConfig):

  def server[F[_]: Async: Network: LoggerFactory: Logger](service: UserManagement[F]): Resource[F, Server] =
    for {
      host <- Resource.eval(
                Async[F].fromOption(
                  Host.fromString(config.host),
                  new IllegalArgumentException(s"Wrong host value: ${config.host}")
                )
              )
      port <- Resource.eval(
                Async[F].fromOption(
                  Port.fromInt(config.port),
                  new IllegalArgumentException(s"Wrong port value: ${config.port}")
                )
              )
      app <- EmberServerBuilder
               .default[F]
               .withHost(host)
               .withPort(port)
               .withHttpApp(Routes.make[F](service))
               .build
    } yield app
