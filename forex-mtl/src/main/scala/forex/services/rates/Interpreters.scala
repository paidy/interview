package forex.services.rates

import cats.Applicative
import cats.effect.Concurrent
import forex.client.OneFrameClient
import forex.domain.Rate
import interpreters._
import scalacache.Cache

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_]: Concurrent](oneFrameHttpClient: OneFrameClient[F], rateCache: Cache[Rate]): Algebra[F] = new OneFrameService[F](oneFrameHttpClient, rateCache)
}
