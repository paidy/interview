package forex.cache.rates

import cats.data.OptionT
import cats.effect.{Async, Sync}
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import forex.model.config.CacheConfig
import forex.model.domain.Rate
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging


class RatesCache[F[_] : Async](
                                config: CacheConfig
                              ) extends Algebra[F] with LazyLogging {

  private val cache: Cache[Rate.Pair, Rate] = Scaffeine()
    .expireAfterWrite(config.expireTimeout)
    .build[Rate.Pair, Rate]()

  override def update(ratePairs: Seq[Rate]): F[Unit] = Sync[F]
    .delay(ratePairs.map(r => (r.pair, r)).toMap)
    .map { pairsMap =>
      cache.putAll(pairsMap)
      pairsMap
    }
    .map { pairsMap =>
      logger.info(s"Rates cache updated with ${pairsMap.size} new values")
    }

  override def get(ratePair: Rate.Pair): OptionT[F, Rate] = OptionT(Sync[F]
    .delay(cache.getIfPresent(ratePair))
    .map { rate =>
      logger.info(s"Read from rates cache, got: $rate")
      rate
    })
}

object RatesCache {

  def apply[F[_] : Async](
                           config: CacheConfig
                         ): Algebra[F] = new RatesCache[F](config)
}
