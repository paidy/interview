package forex.config


import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrame: OneFrameConfig,
    rateLimit: RateLimitConfig,
    redis: RedisConfig
)

case class RedisConfig(
    host: String,
    port: Int
)
case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration,
    tokens: List[String]
)

case class Token(values: List[String], windowSize: FiniteDuration, limitPerToken: Int)

case class HttpOneFrameConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration,
    token: Token
)

case class OneFrameConfig(http: HttpOneFrameConfig, ratesRefresh: FiniteDuration)

case class RateLimitConfig(limitPerToken: Int, windowSize: FiniteDuration)
