package forex.services.rates.interpreters

import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

object Protocol {
  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val timestampEncoder: Decoder[Timestamp] =
    deriveConfiguredDecoder[Timestamp]

  implicit val priceEncoder: Decoder[Price] = deriveConfiguredDecoder[Price]

  implicit val currencyEncoder: Decoder[Currency] = Decoder.decodeString.map(Currency.fromString)

  implicit val reponseDecoder: Decoder[OneFrameResponse] = deriveConfiguredDecoder[OneFrameResponse]
}
