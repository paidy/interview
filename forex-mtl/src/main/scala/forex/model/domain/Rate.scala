package forex.model.domain


final case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)


object Rate {
  final case class Pair(
      from: Currency.Value,
      to: Currency.Value
  ) {
    lazy val joined: String = from.toString + to.toString
  }
}