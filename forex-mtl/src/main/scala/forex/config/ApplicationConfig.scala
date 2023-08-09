package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    storage: StorageConfig
)

case class StorageConfig(expireAfter: FiniteDuration)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)
