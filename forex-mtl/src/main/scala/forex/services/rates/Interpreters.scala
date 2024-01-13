package forex.services.rates

import cats.Applicative
import cats.effect.Async
import forex.cache.CurrencyRateCacheAlgebra
import forex.config.OneFrameConfig
import forex.services.rates.interpreters._
import forex.services.rates.token.TokenProvider
import sttp.client3.{Identity, SttpBackend}

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_]: Async](config: OneFrameConfig,
                        backend: SttpBackend[Identity, Any],
                        cache: CurrencyRateCacheAlgebra[F],
                        tokenProvider: TokenProvider[F]): Algebra[F] =
    OneFrameService[F](config, backend, cache, tokenProvider)
}
