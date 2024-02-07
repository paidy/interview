package forex.domain

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
  object Pair {
    val stringify = (pair: Rate.Pair) => s"${pair.to}${pair.from}"
  }
}
