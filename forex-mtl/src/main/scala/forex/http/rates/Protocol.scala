package forex.http
package rates

import cats.effect.IO
import cats.syntax.either._
import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import forex.services.rates.Interpreters.OneFrameResponse
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import org.http4s.EntityDecoder

import java.time.OffsetDateTime

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

  implicit val oneFrameRateDecoder: Decoder[OneFrameResponse] = deriveConfiguredDecoder

  implicit val decodeInstant: Decoder[OffsetDateTime] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(OffsetDateTime.parse(str)).leftMap(t => s"Failed to parse OffsetDateTime: ${str}, due to ${t.getMessage}")
  }

}
