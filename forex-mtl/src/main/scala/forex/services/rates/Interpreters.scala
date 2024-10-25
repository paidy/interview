package forex.services.rates

import cats.Applicative
import cats.effect.Concurrent
import forex.client.OneFrameClient
import forex.config.{ CacheConfig, SchedulerConfig }
import forex.domain.Rate
import interpreters._
import scalacache.Cache

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_]: Concurrent](oneFrameHttpClient: OneFrameClient[F],
                             rateCache: Cache[Rate],
                             cacheConfig: CacheConfig,
                             schedulerConfig: SchedulerConfig): Algebra[F] =
    new OneFrameService[F](oneFrameHttpClient, rateCache, cacheConfig, schedulerConfig)
}
