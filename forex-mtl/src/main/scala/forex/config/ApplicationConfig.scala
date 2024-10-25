package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
                              interpreter: String,
                              http: HttpConfig,
                              oneFrameSvc: OneFrameConfig,
                              redis: RedisConfig
                            )

case class HttpConfig(
                       host: String,
                       port: Int,
                       timeout: FiniteDuration
                     )

case class OneFrameConfig(
                           http: HttpConfig,
                           token: String,
                         )

case class RedisConfig(
                      host: String,
                      port: Int,
                      expire: FiniteDuration
                      )