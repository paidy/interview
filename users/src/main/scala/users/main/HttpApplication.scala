package users.main

import akka.http.scaladsl.Http
import akka.http.scaladsl.settings.ServerSettings
import cats.data.Reader
import users.config.{HttpApplicationConfig, HttpConfig}

object HttpApplication {
  val reader: Reader[(HttpConfig, AkkaSystem, Apis), HttpApplication] =
    Reader((HttpApplication.apply _).tupled)

  val fromApplicationConfig: Reader[HttpApplicationConfig, HttpApplication] = {
    (for {
      httpConfig <- HttpConfig.fromApplicationConfig
      akkaSystem <- AkkaSystem.fromApplicationConfig
      apis <- Apis.fromApplicationConfig.local[HttpApplicationConfig](_.application)
    } yield (httpConfig, akkaSystem, apis)) andThen reader
  }

}

final case class HttpApplication(
  httpConfig: HttpConfig,
  akkaSystem: AkkaSystem,
  apis: Apis
) {

  import akkaSystem._

  private val settings = ServerSettings(actorSystem)

  Http().bindAndHandle(
    handler = apis.userRestApi.routes,
    interface = httpConfig.host,
    port = httpConfig.port,
    settings = settings.withTimeouts(
      settings.timeouts.withRequestTimeout(
        httpConfig.requestTimeout))
  )
}
