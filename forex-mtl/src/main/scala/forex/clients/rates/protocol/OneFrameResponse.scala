package forex.clients.rates.protocol

import cats.implicits.toFunctorOps
import forex.domain.{ Price, Timestamp }
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

import java.time.OffsetDateTime

sealed trait OneFrameResponse

object OneFrameResponse {

  case class SuccessResponse(rates: List[OneFrameRate]) extends OneFrameResponse

  case class ErrorResponse(error: String) extends OneFrameResponse

  private implicit val circeConfiguration: Configuration = Configuration.default.withSnakeCaseMemberNames

  private implicit val priceDecoder: Decoder[Price] = _.value.as[BigDecimal].map(Price(_))
  private implicit val timestampDecoder: Decoder[Timestamp] =
    _.value.as[String].map(strTime => Timestamp.apply(OffsetDateTime.parse(strTime)))

  private implicit val oneFrameRateDecoder: Decoder[OneFrameRate] = deriveConfiguredDecoder[OneFrameRate]

  private implicit val oneRateResponseSuccessDecoder: Decoder[OneFrameResponse.SuccessResponse] =
    _.as[List[OneFrameRate]].map(SuccessResponse)

  private implicit val oneRateResponseErrorDecoder: Decoder[OneFrameResponse.ErrorResponse] =
    deriveConfiguredDecoder[OneFrameResponse.ErrorResponse]

  implicit val oneRateResponseDecoder: Decoder[OneFrameResponse] =
    List[Decoder[OneFrameResponse]](
      Decoder[OneFrameResponse.SuccessResponse].widen,
      Decoder[OneFrameResponse.ErrorResponse].widen
    ).reduceLeft(_ or _)

}
