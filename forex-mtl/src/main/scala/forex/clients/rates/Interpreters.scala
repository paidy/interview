package forex.clients.rates

import cats.effect.Async
import forex.clients.rates.interpeters.OneFrameClientLive
import forex.config.ApplicationConfig.OneFrameClientConfig
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend

object Interpreters {

  def live[F[_]: Async](config: OneFrameClientConfig,
                        backend: SttpBackend[F, Fs2Streams[F]]): OneFrameClientAlgebra[F] =
    new OneFrameClientLive[F](config, backend)

}
