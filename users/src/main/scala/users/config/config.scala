package users.config

import cats.data._

import scala.concurrent.duration.Duration

case class ApplicationConfig(
    executors: ExecutorsConfig,
    services: ServicesConfig
)

case class HttpApplicationConfig(
  application: ApplicationConfig,
  akka: AkkaConfig,
  http: HttpConfig
)

case class ExecutorsConfig(
    services: ExecutorsConfig.ServicesConfig
)

object ExecutorsConfig {
  val fromApplicationConfig: Reader[ApplicationConfig, ExecutorsConfig] =
    Reader(_.executors)

  case class ServicesConfig(
      parallellism: Int
  )
}

case class ServicesConfig(
    users: ServicesConfig.UsersConfig
)

object ServicesConfig {
  val fromApplicationConfig: Reader[ApplicationConfig, ServicesConfig] =
    Reader(_.services)

  case class UsersConfig(
      failureProbability: Double,
      timeoutProbability: Double
  )
}


case class AkkaConfig(
  name: String
)

object AkkaConfig {
  val fromApplicationConfig: Reader[HttpApplicationConfig, AkkaConfig] =
    Reader(_.akka)
}

case class HttpConfig(
  port: Int,
  host: String,
  requestTimeout: Duration
)

object HttpConfig {
  val fromApplicationConfig: Reader[HttpApplicationConfig, HttpConfig] =
    Reader(_.http)
}
