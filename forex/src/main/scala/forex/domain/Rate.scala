package forex.domain

import cats.Show
import io.circe._
import io.circe.generic.semiauto._
import cats.syntax.either._

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

    def fromString(s: String): Either[String, Pair] = {
      (for {
        from <- Either.catchNonFatal(Currency.fromString(s.take(3)))
        to <- Either.catchNonFatal(Currency.fromString(s.takeRight(3)))
      } yield Pair(from, to)).leftMap(t => s)
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
