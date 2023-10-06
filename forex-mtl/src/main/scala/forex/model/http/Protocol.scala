package forex.model.http

import forex.model.domain.{Currency, Price, Rate, Timestamp}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe._

import java.time.OffsetDateTime
import scala.util.Try


object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class OneFrameRate(
                                 from: Currency.Value,
                                 to: Currency.Value,
                                 bid: BigDecimal,
                                 ask: BigDecimal,
                                 price: Price,
                                 timeStamp: Timestamp
                               )

  final case class GetApiResponse(
                                   from: Currency.Value,
                                   to: Currency.Value,
                                   price: Price,
                                   timestamp: Timestamp
                                 )

  implicit val currencyEncoder: Encoder[Currency.Value] =
    Encoder.instance[Currency.Value] {
      Currency.show.show _ andThen Json.fromString
    }

  implicit val currencyDecoder: Decoder[Currency.Value] =
    Decoder.instance[Currency.Value](c => c.as[String]
      .flatMap(str => Currency.fromString(str).toEither
      .left.map(err => DecodingFailure(err.getMessage, c.history))))

  implicit val pairEncoder: Encoder[Rate.Pair] =
    deriveConfiguredEncoder[Rate.Pair]

  implicit val priceEncoder: Encoder[Price] =
    Encoder.instance[Price] (v => Json.fromBigDecimal(v.value))

  implicit val priceDecoder: Decoder[Price] =
    Decoder.instance[Price](_.as[BigDecimal].map(Price(_)))

  implicit val timestampEncoder: Encoder[Timestamp] =
    Encoder.instance[Timestamp] (v => Json.fromString(v.value.toString))

  implicit val timestampDecoder: Decoder[Timestamp] =
    Decoder.instance[Timestamp](c => c.as[String]
      .flatMap(str => Try(Timestamp(OffsetDateTime.parse(str))).toEither
        .left.map(err => DecodingFailure(err.getMessage, c.history))))

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]

  implicit val oneFrameRateEncoder: Encoder[OneFrameRate] =
    deriveConfiguredEncoder[OneFrameRate]

  implicit val oneFrameRateDecoder: Decoder[OneFrameRate] =
    deriveConfiguredDecoder[OneFrameRate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]

  implicit val responseDecoder: Decoder[GetApiResponse] =
    deriveConfiguredDecoder[GetApiResponse]
}
