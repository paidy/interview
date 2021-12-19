package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    pollDuration: FiniteDuration
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)
