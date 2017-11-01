package forex.interfaces.api.rates

import java.time.OffsetDateTime

import forex.domain._
import io.circe._
import io.circe.generic.semiauto._

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

  object GetApiResponse {
    implicit val encoder: Encoder[GetApiResponse] = deriveEncoder[GetApiResponse]
  }

}
