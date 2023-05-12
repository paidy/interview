package forex.config

import forex.config.ApplicationConfig._

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpServerConfig,
    cache: CacheConfig,
    database: DatabaseConfig,
    oneFrame: OneFrameClientConfig
)

object ApplicationConfig {
  case class HttpServerConfig(
      host: String,
      port: Int,
      timeout: FiniteDuration
  )

  case class CacheConfig(ttl: FiniteDuration)

  case class DatabaseConfig(maximumPoolSize: Int,
                            driver: String,
                            url: String,
                            user: String,
                            password: String,
                            connectionTimeout: FiniteDuration,
                            validationTimeout: FiniteDuration)

  case class OneFrameClientConfig(host: String, token: String)
}
