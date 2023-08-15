package forex.domain

import cats.Show
import cats.syntax.all._

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {
  object Pair {
    implicit def show: Show[Pair] = pair => pair.from.show + pair.to.show
  }

  final case class Pair(
      from: Currency,
      to: Currency
  )
}
