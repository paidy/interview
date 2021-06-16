package forex.http
package rates

import java.time.OffsetDateTime

import forex.client.errors.OneFrameClientResponseError
import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.generic.semiauto.deriveDecoder

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class GetApiRequest(
      from: Currency,
      to: Currency
  )

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { show.show _ andThen Json.fromString }

  implicit val pairEncoder: Encoder[Pair] =
    deriveConfiguredEncoder[Pair]

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]

  implicit val rateDecoder: Decoder[Rate] = (cursor: HCursor) =>
    for {
      from <- cursor.downField("from").as[Currency]
      to <- cursor.downField("to").as[Currency]
      price <- cursor.downField("price").as[BigDecimal]
      timestamp <- cursor.downField("time_stamp").as[OffsetDateTime]
    } yield {
      Rate(Rate.Pair(from, to), Price(price), Timestamp(timestamp))
  }

  implicit val errorResponseDecoder: Decoder[OneFrameClientResponseError] = deriveDecoder[OneFrameClientResponseError]

  implicit val rateOrErrorDecoder: Decoder[Either[OneFrameClientResponseError, List[Rate]]] =
    errorResponseDecoder.either(Decoder.decodeList(rateDecoder))

}
