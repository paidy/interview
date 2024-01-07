package forex.http.rates

import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  implicit val timestampEncoder: Codec[Timestamp] =
    deriveConfiguredCodec[Timestamp]

  implicit val priceEncoder: Codec[Price] = deriveConfiguredCodec[Price]

  implicit val currencyEncoder: Codec[Currency] =
    Codec.from(
      Decoder.decodeString.map(Currency.fromString),
      Encoder.instance[Currency] { show.show _ andThen Json.fromString }
    )

  implicit val pairEncoder: Codec[Pair] =
    deriveConfiguredCodec[Pair]

  implicit val rateEncoder: Codec[Rate] =
    deriveConfiguredCodec[Rate]

  implicit val responseEncoder: Codec[GetApiResponse] =
    deriveConfiguredCodec[GetApiResponse]

}
