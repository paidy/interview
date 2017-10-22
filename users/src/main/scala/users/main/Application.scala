package users.main

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import cats.data._
import users.config._
import users.api.UsersRestApi

import scala.concurrent.Future

object Application {

  val reader: Reader[Apis, Application] =
    Reader(Application(_))

  val fromApplicationConfig: Reader[ApplicationConfig, Application] =
    Apis.fromApplicationConfig andThen reader

}

case class Application(
  apis: Apis
)
