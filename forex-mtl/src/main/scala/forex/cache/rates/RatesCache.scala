package forex.cache.rates

import cats.Functor
import forex.config.CacheConfig
import forex.domain.Rate
import forex.programs.rates.errors.Error


class RatesCache[F[_] : Functor](
                                       config: CacheConfig
                                     ) extends Algebra[F] {

  override def update(ratePairs: Seq[Rate]): F[Error Either Unit] = ???

  override def get(ratePair: Rate.Pair): F[Error Either Rate] = ???
}

object RatesCache {

  def apply[F[_] : Functor](
                             config: CacheConfig
                           ): Algebra[F] = new RatesCache[F](config)

}
