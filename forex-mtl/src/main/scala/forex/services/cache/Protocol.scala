package forex.services.cache

import io.circe._
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto.{ deriveUnwrappedDecoder, deriveUnwrappedEncoder }
import forex.domain.Currency.show
import forex.domain.Rate.Pair
import forex.domain._


object Protocol {

  implicit val currencyDecoder: Decoder[Currency] = Decoder.decodeString.map(Currency.fromString)
  implicit val currencyEncoder: Encoder[Currency] = Encoder.instance[Currency] {show.show _ andThen Json.fromString}

  implicit val pairEncoder: Encoder[Pair] = deriveEncoder
  implicit val pairDecoder: Decoder[Pair] = deriveDecoder

  implicit val timestampDecoder: Decoder[Timestamp] = deriveUnwrappedDecoder[Timestamp]
  implicit val timestampEncoder: Encoder[Timestamp] = deriveUnwrappedEncoder[Timestamp]

  implicit val priceDecoder: Decoder[Price] = deriveUnwrappedDecoder[Price]
  implicit val priceEncoder: Encoder[Price] = deriveUnwrappedEncoder[Price]

  implicit val rateDecoder: Decoder[Rate] = deriveDecoder
  implicit val rateEncoder: Encoder[Rate] = deriveEncoder

}
