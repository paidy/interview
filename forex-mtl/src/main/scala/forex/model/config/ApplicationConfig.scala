package forex.model.config

import scala.concurrent.duration.FiniteDuration


final case class ApplicationConfig(
                                    http: HttpConfig,
                                    oneFrameClient: OneFrameClientConfig,
                                    program: ProgramConfig
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

final case class ProgramConfig(
                                oneFrameToken: String,
                                cacheExpireTimeout: FiniteDuration
                              )

