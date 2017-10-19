package users.main

import cats.data._
import users.config._

object Application {
  val reader: Reader[Services, Application] =
    Reader(Application.apply)

  val fromApplicationConfig: Reader[ApplicationConfig, Application] =
    Services.fromApplicationConfig andThen reader
}

case class Application(
    services: Services
)
