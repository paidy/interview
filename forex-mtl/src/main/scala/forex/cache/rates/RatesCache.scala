package forex.cache.rates

import cats.Functor
import forex.model.config.CacheConfig
import forex.model.domain.Rate


class RatesCache[F[_] : Functor](
                                       config: CacheConfig
                                     ) extends Algebra[F] {

  override def update(ratePairs: Seq[Rate]): F[Unit] = ???

  override def get(ratePair: Rate.Pair): F[Rate] = ???
}

object RatesCache {

  def apply[F[_] : Functor](
                             config: CacheConfig
                           ): Algebra[F] = new RatesCache[F](config)

}
