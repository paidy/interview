package forex.domain.model

import cats.implicits.toShow

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  ) {
    override def toString: String =
      s"${from.show}${to.show}"
  }
}
