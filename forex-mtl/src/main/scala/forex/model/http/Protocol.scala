package forex.model.http

import forex.model.domain.{Currency, Price, Rate, Timestamp}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe._


object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class OneFrameRate(
                                 from: Currency.Value,
                                 to: Currency.Value,
                                 bid: BigDecimal,
                                 ask: BigDecimal,
                                 price: Price,
                                 timestamp: Timestamp
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
    deriveConfiguredEncoder[Price]

  implicit val priceDecoder: Decoder[Price] =
    deriveConfiguredDecoder[Price]

  implicit val timestampEncoder: Encoder[Timestamp] =
    deriveConfiguredEncoder[Timestamp]

  implicit val timestampDecoder: Decoder[Timestamp] =
    deriveConfiguredDecoder[Timestamp]

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]

  implicit val oneFrameRateEncoder: Encoder[OneFrameRate] =
    deriveConfiguredEncoder[OneFrameRate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]

  implicit val responseDecoder: Decoder[GetApiResponse] =
    deriveConfiguredDecoder[GetApiResponse]
}
