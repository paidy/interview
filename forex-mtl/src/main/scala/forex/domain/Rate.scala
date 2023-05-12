package forex.domain

import forex.persistence.entity.RateEntity

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  )

  def fromRateEntity(entity: RateEntity): Rate = Rate(Pair(entity.from, entity.to), entity.price, entity.timestamp)
}
