package forex.cache.rates

import forex.cache.CaffeineCustomCache
import forex.cache.rates.RatesCache.getRateKey
import forex.domain.Rate

class RatesCache private (private val cache: CaffeineCustomCache[String, Rate]) {
  def getRate(from: String, to: String): Option[Rate] = {
    cache.get(getRateKey(from, to))
  }

  def setRate(from: String, to: String, rate: Rate): Unit = {
    cache.put(getRateKey(from, to), rate)
  }
}

object RatesCache {
  private val instance: RatesCache = new RatesCache(CaffeineCustomCache[String, Rate](10000, 300))

  private def getRateKey(from: String, to: String): String = {
    s"rate:$from:$to"
  }

  def getInstance: RatesCache = instance
}
