package forex.infrastructure.cache

import forex.services.rates.RatesCache
import forex.domain.Rate
import com.github.blemale.scaffeine.Cache

class RatesCacheCaffiene(val cache: Cache[String, Rate]) extends RatesCache {
  override def get(pair: Rate.Pair): Option[Rate] = {
    return cache.getIfPresent(getKey(pair))
  }

  override def setAll(rates: Set[Rate]): Unit = {
    var values: Map[String, Rate] = Map()

    for (rate <- rates) {
      values = values + (getKey(rate.pair) -> rate)
    }

    cache.putAll(values)
  }

  private def getKey(pair: Rate.Pair): String = {
    val from = pair.from.toString()
    val to = pair.to.toString()
    return s"rate:pair:$from:$to"
  }
}
