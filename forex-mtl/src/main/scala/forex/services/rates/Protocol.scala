package forex.services.rates

import cats.data.NonEmptyList
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  case class OneFrameRequest(
    pairs: NonEmptyList[Rate.Pair]
  )

  case class ExchangeRate(
    from: Currency,
    to: Currency,
    bid: BigDecimal,
    ask: BigDecimal,
    price: Price,
    timeStamp: Timestamp
  ) {
    def toRate: Rate = Rate(
      Rate.Pair(from, to),
      price,
      timeStamp
    )
  }

  case class OneFrameResponse(
    rates: List[ExchangeRate]
  ) {
    def toPairDetails: List[Rate] = rates.map(_.toRate)
  }

  implicit val currencyDecoder: Decoder[Currency] = Decoder.decodeString.map(Currency.fromString)
  implicit val timestampDecoder: Decoder[Timestamp] = deriveUnwrappedDecoder[Timestamp]
  implicit val priceDecoder: Decoder[Price] = deriveUnwrappedDecoder[Price]
  implicit val exchangeRateDecoder: Decoder[ExchangeRate] = deriveConfiguredDecoder[ExchangeRate]
  implicit val oneFrameResponseDecoder: Decoder[OneFrameResponse] = deriveConfiguredDecoder[OneFrameResponse]
}
