package forex.services.cache

import forex.domain.Rate

trait Algebra {
  def get(pair: Rate.Pair): Option[Rate]
  def setOne(rate: Rate): Boolean
  def setAll(rates: List[Rate]): Boolean
}
