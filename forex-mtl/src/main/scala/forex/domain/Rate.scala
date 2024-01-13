package forex.domain

import io.circe.{Decoder, Encoder, HCursor}

import java.time.OffsetDateTime

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

  implicit val rateDecoder: Decoder[Rate] = (cursor: HCursor) =>
    for {
      from <- cursor.downField("from").as[Currency]
      to <- cursor.downField("to").as[Currency]
      price <- cursor.downField("price").as[BigDecimal]
      timestamp <- cursor.downField("time_stamp").as[OffsetDateTime]
    } yield {
      Rate(Rate.Pair(from, to), Price(price), Timestamp(timestamp))
    }

  implicit val rateEncoder: Encoder[Rate] = Encoder.forProduct4(
    "from",
    "to",
    "price",
    "time_stamp"
  )(rate => (rate.pair.from, rate.pair.to, rate.price.value, rate.timestamp.value))
}
