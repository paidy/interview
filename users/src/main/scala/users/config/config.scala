package users.config

import cats.data._

case class ApplicationConfig(
    executors: ExecutorsConfig,
    services: ServicesConfig,
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
  val fromApplicationConfig: Reader[ApplicationConfig, AkkaConfig] =
    Reader(_.akka)
}

case class HttpConfig(
  port: Int,
  host: String
)

object HttpConfig {
  val fromApplicationConfig: Reader[ApplicationConfig, HttpConfig] =
    Reader(_.http)
}
