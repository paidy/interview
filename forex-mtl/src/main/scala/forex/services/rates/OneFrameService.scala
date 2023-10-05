package forex.services.rates

import cats.Functor
import forex.cache.RatesCache
import forex.clients.RatesClient
import forex.config.OneFrameServiceConfig


class OneFrameService[F[_] : Functor](
                                       config: OneFrameServiceConfig,
                                       ratesClient: RatesClient[F],
                                       ratesCache: RatesCache[F]
                                     ) extends Algebra[F] {

  // TODO
}

object OneFrameService {

  def apply[F[_] : Functor](
                             config: OneFrameServiceConfig, ratesClient: RatesClient[F], ratesCache: RatesCache[F]
                           ): Algebra[F] = new OneFrameService[F](config, ratesClient, ratesCache)

}
