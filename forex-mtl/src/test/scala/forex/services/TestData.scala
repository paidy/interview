package forex.services

import forex.domain.{ Currency, Price, Rate, Timestamp }

import scala.util.Random

trait TestData {
  def genRates =
    for {
      from <- Currency.values
      to <- Currency.values if to != from
    } yield Rate(Rate.Pair(from, to), Price(Random.nextDouble()), Timestamp.now)
}
