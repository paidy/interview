package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrame: OneFrameConfig,
    ratesIngestor: RatesIngestorConfig
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class OneFrameConfig(
    token: String,
    http: HttpConfig
)

case class RatesIngestorConfig(
    expireAfter: FiniteDuration,
    refreshInterval: FiniteDuration
)
