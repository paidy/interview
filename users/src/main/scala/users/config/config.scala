package users.config

import cats.data._

case class ApplicationConfig(
    executors: ExecutorsConfig,
    services: ServicesConfig
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
