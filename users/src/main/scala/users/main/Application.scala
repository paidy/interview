package users.main

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import cats.data._
import users.config._
import users.api.UsersRestApi

import scala.concurrent.Future

object Application {

  val reader: Reader[(HttpConfig, AkkaSystem, Apis), Application] =
    Reader((Application.apply _).tupled)

  val fromApplicationConfig: Reader[ApplicationConfig, Application] =
    (for {
      httpConfig <- HttpConfig.fromApplicationConfig
      akkaSystem <- AkkaSystem.fromApplicationConfig
      apis <- Apis.fromApplicationConfig
    } yield (httpConfig, akkaSystem, apis)) andThen reader

}

case class Application(
    httpConfig: HttpConfig,
    akkaSystem: AkkaSystem,
    apis: Apis
) {

  import akkaSystem._

  def start(): Future[ServerBinding] = {

    Http().bindAndHandle(
      handler = apis.userRestApi.routes,
      interface = httpConfig.host,
      port = httpConfig.port)
  }
}
