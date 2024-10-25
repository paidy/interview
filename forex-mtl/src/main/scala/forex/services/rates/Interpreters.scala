package forex.services.rates

import cats.{ Applicative, Monad }
import forex.config.ProviderConfig
import interpreters._
import sttp.client3.SttpBackend

object Interpreters {
  def live[F[_]: Monad](config: ProviderConfig, backend: SttpBackend[F, _]): Algebra[F] =
    new OneFrameLive[F](config, backend)
}
