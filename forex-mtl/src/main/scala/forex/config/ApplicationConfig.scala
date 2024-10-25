package forex.config

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    storage: StorageConfig,
    provider: ProviderConfig
)

case class StorageConfig(expireAfter: FiniteDuration, apiLimit: Int) {

  /**
    * Ensures that we don't exceed API queries limit with provided pull period
    */
  require(
    24 * 60 / expireAfter.toUnit(TimeUnit.MINUTES) < apiLimit,
    s"The service might exceed API limit with an expire period ${expireAfter.toString()}"
  )
}

case class ProviderConfig(uri: String, token: String)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)
