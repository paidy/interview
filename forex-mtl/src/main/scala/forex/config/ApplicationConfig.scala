package forex.config

import scala.concurrent.duration.FiniteDuration


case class ApplicationConfig(
                              http: HttpConfig,
                              oneFrameClient: OneFrameClientConfig,
                              oneFrameService: OneFrameServiceConfig,
                              cache: CacheConfig,
                              program: ProgramConfig
                            )


case class HttpConfig(
                       host: String,
                       port: Int,
                       timeout: FiniteDuration
                     )


case class OneFrameClientConfig(
                                 host: String,
                                 port: Int,
                                 timeout: FiniteDuration
                               )


case class OneFrameServiceConfig(
                                  numRetry: Int,
                                  retryTimeout: FiniteDuration,
                                  oneFrameTokens: List[String],
                                  ratesRefreshTimeout: FiniteDuration
                                )


case class CacheConfig(
                        expireTimeout: FiniteDuration
                      )


case class ProgramConfig(

                        )
