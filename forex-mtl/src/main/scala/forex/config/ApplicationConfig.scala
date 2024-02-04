package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
                              interpreter: String,
                              http: HttpConfig,
                              oneFrameSvc: OneFrameConfig
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