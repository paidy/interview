package forex.config

import scala.concurrent.duration.FiniteDuration

final case class ApplicationConfig(
    http: HttpConfig,
    oneFrame: OneFrameConfig,
    cache: CacheConfig
)

final case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

final case class OneFrameConfig(
                                 url: String,
                                 token: String
                               )

final case class CacheConfig(
                              oneFrameExpiry: Int
                            )


