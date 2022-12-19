package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
  httpServer: HttpServerConfig,
  httpClient: HttpClientConfig,
  oneFrame: OneFrameConfig
)

case class HttpServerConfig(
  host: String,
  port: Int,
  timeout: FiniteDuration
)

case class HttpClientConfig(
  timeout: FiniteDuration,
  idleTimePool: FiniteDuration
)

case class OneFrameConfig(
  host: String,
  port: Int,
  token: String
)
