package users.config

import cats.*
import cats.data.*
import cats.implicits.*

case class ApplicationConfig(
  executors: ExecutorsConfig,
  services: ServicesConfig,
  httpConfig: HttpConfig = HttpConfig.default
)

case class ExecutorsConfig(
  services: ExecutorsConfig.ServicesConfig
)

object ExecutorsConfig {

  def fromApplicationConfig[F[_]: Applicative]: ReaderT[F, ApplicationConfig, ExecutorsConfig] =
    ReaderT(_.executors.pure)

  case class ServicesConfig(
    parallellism: Int
  )
}

case class ServicesConfig(
  users: ServicesConfig.UsersConfig
)

object ServicesConfig {

  def fromApplicationConfig[F[_]: Applicative]: ReaderT[F, ApplicationConfig, ServicesConfig] =
    ReaderT(_.services.pure)

  case class UsersConfig(
    failureProbability: Double,
    timeoutProbability: Double
  )
}

object HttpConfig:
  val default = HttpConfig(host = "localhost", port = 8080)

case class HttpConfig(host: String, port: Int)
