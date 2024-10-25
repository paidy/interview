package forex.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class OneFrameCurrencyInformation(from: String, to: String, bid: Double, ask: Double, price: Double, time_stamp: String)

object OneFrameCurrencyInformation {
  implicit val oneFrameCurrencyInformationDecoder: Decoder[OneFrameCurrencyInformation] = deriveDecoder
  implicit val oneFrameCurrencyInformationEncoder: Encoder[OneFrameCurrencyInformation] = deriveEncoder
}
