package forex.cache

import forex.domain.Rate
import forex.cache.errors.Error
trait CurrencyRateCacheAlgebra[F[_]] {
  def getRates(key: String): F[Error Either Rate]
  def updateRates(key: String, rate: Rate, timeoutInSeconds: Int): F[String]
}
