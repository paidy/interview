package forex.http
package rates

import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._
import forex.programs.rates.errors.{ Error => ProgramError, showMessage }
import io.circe._
import io.circe.Encoder
import io.circe.generic.semiauto._
import io.circe.java8.time._

object Protocol {

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
    deriveEncoder[Pair]

  implicit val rateEncoder: Encoder[Rate] =
    deriveEncoder[Rate]

  implicit val responseEncoder: Encoder[GetApiResponse] =
    deriveEncoder[GetApiResponse]

  implicit val errorEncoder: Encoder[ProgramError] = Encoder.instance[ProgramError] {
    showMessage.show _ andThen Json.fromString
  }

  implicit val eitherEncoder: Encoder[ProgramError Either GetApiResponse] = Encoder.encodeEither("error", "success")

}
