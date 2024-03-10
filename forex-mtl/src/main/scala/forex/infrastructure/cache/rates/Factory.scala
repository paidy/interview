package forex.infrastructure.cache.rates

import forex.services.rates.RatesCache
import forex.infrastructure.cache.Caffiene
import forex.domain.Rate
import scala.concurrent.duration.FiniteDuration

object Factory {
  def create(expireTime: FiniteDuration, maxSize: Long): RatesCache = {
    return new RatesCacheCaffiene(
      Caffiene.newInstance[String, Rate](expireTime, maxSize)
    )
  }
}
