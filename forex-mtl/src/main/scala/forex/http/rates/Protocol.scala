package forex.http
package rates

import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import cats.implicits._

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

  final case class ApiError(
      errorType: String,
      errorMsg: String
  )

  implicit val currencyEncoder: Encoder[Currency] =
    Encoder.instance[Currency] { show.show _ andThen Json.fromString }

  implicit val currencyDecoder: Decoder[Currency] =
    Decoder.instance[Currency](
      _.value.asString
        .toRight(DecodingFailure("Currency is not a String.", Nil))
        .flatMap(
          (Currency.fromString _).andThen(_.toEither.leftMap(nel => DecodingFailure(nel.head.getMessage(), Nil)))
        )
    )

  implicit val pairEncoder: Encoder[Pair] =
    deriveConfiguredEncoder[Pair]

  implicit val rateEncoder: Encoder[Rate] =
    deriveConfiguredEncoder[Rate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveConfiguredEncoder[GetApiResponse]

  implicit val errorEncoder: Encoder[ApiError] =
    deriveConfiguredEncoder[ApiError]
}
