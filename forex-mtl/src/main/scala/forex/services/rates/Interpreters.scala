package forex.services.rates

import cats.Applicative
import interpreters._
import forex.thirdPartyApi.oneFrameApiClient

object Interpreters {
  def oneFrame[F[_]: Applicative]: Algebra[F] = new OneFrameInterpreter[F](oneFrameApiClient, RatesCache.ratesCache)
}
