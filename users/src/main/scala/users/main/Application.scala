package users.main

import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.*

import users.config.*

object Application:

  def reader[F[_]: Async]: ReaderT[F, Services[F], Application[F]] = ReaderT(Application[F].apply(_).pure)

  def fromApplicationConfig[F[_]: Async]: ReaderT[F, ApplicationConfig, Application[F]] =
    Services.fromApplicationConfig[F].andThen(reader)

case class Application[F[_]: Async](services: Services[F])
