package forex.repos.rates.interpreters

import forex.domain.Currency
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import forex.http.rates.Protocol._

import java.time.OffsetDateTime

object OneFrameProtocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  case class OneFrameRate(
      from: Currency,
      to: Currency,
      bid: BigDecimal,
      ask: BigDecimal,
      price: BigDecimal,
      time_stamp: OffsetDateTime
  )

  case class OneFrameResponse(rates: Seq[OneFrameRate])

  implicit val oneFrameRateDecoder: Decoder[OneFrameRate] =
    deriveConfiguredDecoder[OneFrameRate]

  implicit val oneFrameResponseDecoder: Decoder[OneFrameResponse] =
    deriveConfiguredDecoder[OneFrameResponse]
}
