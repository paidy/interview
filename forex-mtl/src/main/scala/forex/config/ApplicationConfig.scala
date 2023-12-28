package forex.config

import scala.concurrent.duration.FiniteDuration

final case class ApplicationConfig(
    http: HttpConfig,
)

final case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)
