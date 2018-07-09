package forex.services.cache

import forex.domain.Rate
import org.atnos.eff.Eff
import org.atnos.eff.memo._

object Interpreters {

  def ratesCache[R](implicit
                    m: _memo[R]
                   ): Algebra[Eff[R, ?]] = new RatesCache[R]()
}

final class RatesCache[R](implicit m: _memo[R]) extends Algebra[Eff[R, ?]] {

  override def get(pair: Rate.Pair): Eff[R, Option[Rate]] =
    for {
      r <- getCache.map(cache => cache.get[Rate](pair))
    } yield r

  override def store(rates: Set[Rate]): Eff[R, Unit] =
    for {
      cache <- getCache
    } yield rates.foreach(rate => cache.put(rate.pair, rate))
}
