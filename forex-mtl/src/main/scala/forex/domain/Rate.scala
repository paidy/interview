package forex.domain

import cats.Show
import cats.implicits._

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
  implicit val showPair: Show[Pair] = Show.show(
    pair => pair.from.show + pair.to.show
  )
}
