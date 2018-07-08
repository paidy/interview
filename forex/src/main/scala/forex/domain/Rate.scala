package forex.domain

import cats.Show
import io.circe._
import io.circe.generic.semiauto._

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
    implicit val encoder: Encoder[Pair] =
      deriveEncoder[Pair]

    implicit def show(implicit currencyShow: Show[Currency]): Show[Pair] = Show.show {
      pair =>
        currencyShow.show(pair.from) + currencyShow.show(pair.to)
    }

    val allSupportedPairs: Set[Pair] =
      for {
        from <- Currency.allCurrencies
        to <- Currency.allCurrencies
        if from != to
      } yield Pair(from, to)
  }

  implicit val encoder: Encoder[Rate] =
    deriveEncoder[Rate]
}
