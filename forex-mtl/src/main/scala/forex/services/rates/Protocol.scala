package forex.services.rates

import cats.data.NonEmptyList
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.extras.semiauto.deriveUnwrappedDecoder


object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

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
    rates: NonEmptyList[ExchangeRate]
  )

  implicit val currencyDecoder: Decoder[Currency] = Decoder.decodeString.map(Currency.withName)
  implicit val timestampDecoder: Decoder[Timestamp] = deriveUnwrappedDecoder[Timestamp]
  implicit val priceDecoder: Decoder[Price] = deriveUnwrappedDecoder[Price]
  implicit val exchangeRateDecoder: Decoder[ExchangeRate] = deriveConfiguredDecoder[ExchangeRate]
  implicit val oneFrameResponseDecoder: Decoder[OneFrameResponse] = deriveUnwrappedDecoder[OneFrameResponse]
}
