package forex.model.config

import scala.concurrent.duration.FiniteDuration


final case class ApplicationConfig(
                              http: HttpConfig,
                              oneFrameClient: OneFrameClientConfig,
                              oneFrameService: OneFrameServiceConfig,
                              cache: CacheConfig
                            )


final case class HttpConfig(
                       host: String,
                       port: Int,
                       timeout: FiniteDuration
                     )


final case class OneFrameClientConfig(
                                 host: String,
                                 port: Int,
                                 timeout: FiniteDuration
                               )


final case class OneFrameServiceConfig(
                                  oneFrameTokens: List[String],
                                  ratesRefreshTimeout: FiniteDuration
                                )


final case class CacheConfig(
                        expireTimeout: FiniteDuration
                      )

