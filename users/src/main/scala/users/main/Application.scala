package users.main

import cats.data.*

import users.config.*

object Application:

  val reader: Reader[Services, Application] = Reader(Application.apply)

  val fromApplicationConfig: Reader[ApplicationConfig, Application] =
    Services.fromApplicationConfig.andThen(reader)

case class Application(
  services: Services
)
