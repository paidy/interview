package forex.domain

import cats.implicits.toShow
case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
){
  override def toString: String = pair.toString + price.value
}

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  ){
    def combine = from.show + to.show
  }
}
