package forex.services.rates

import forex.domain.Rate
import scala.concurrent.duration._
import forex.infrastructure.ratesCacheFactory

trait RatesCache {
  def get(pair: Rate.Pair): Option[Rate]

  def setAll(rates: Set[Rate]): Unit
}

object RatesCache {
  final val ratesCache = ratesCacheFactory.create(3.minute, 100)
}