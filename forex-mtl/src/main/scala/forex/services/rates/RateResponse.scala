package forex.services.rates

import forex.domain.{Currency, Price, Timestamp}
import io.circe.{Decoder, HCursor}

case class RateResponse(
                         from: Currency,
                         to: Currency,
                         bid: BigDecimal,
                         ask: BigDecimal,
                         price: Price,
                         timeStamp: Timestamp
                       )

object RateResponse {
  implicit val currencyDecoder: Decoder[Currency] = Decoder.decodeString.map(Currency.fromString)
  implicit val priceDecoder: Decoder[Price] = Decoder.decodeBigDecimal.map(Price(_))

  implicit val timestampDecoder: Decoder[Timestamp] = Decoder.decodeOffsetDateTime.map(Timestamp(_))

  implicit val rateResponseDecoder: Decoder[RateResponse] = new Decoder[RateResponse] {
    final def apply(c: HCursor): Decoder.Result[RateResponse] =
      for {
        from <- c.downField("from").as[Currency]
        to <- c.downField("to").as[Currency]
        bid <- c.downField("bid").as[BigDecimal]
        ask <- c.downField("ask").as[BigDecimal]
        price <- c.downField("price").as[Price]
        timeStamp <- c.downField("time_stamp").as[Timestamp]
      } yield RateResponse(from, to, bid, ask, price, timeStamp)
  }
}
