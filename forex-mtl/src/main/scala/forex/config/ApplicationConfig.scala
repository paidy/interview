package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrameConfig: OneFrameConfig,
    ratesExpiration: FiniteDuration
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class HttpClient(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class OneFrameConfig(httpClient: HttpClient,
                          retryPolicy: RetryPolicy,
                          staticToken: String,
                          scheduleTime: FiniteDuration)

case class RetryPolicy(maxRetries: Int, delay: FiniteDuration)
