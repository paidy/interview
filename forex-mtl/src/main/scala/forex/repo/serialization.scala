package forex

import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

package object repo {
  implicit val priceEncoder: Encoder[Price] = deriveEncoder
  implicit val priceDecoder: Decoder[Price] = deriveDecoder

  implicit val currencyEncoder: Encoder[Currency] = deriveEncoder
  implicit val currencyDecoder: Decoder[Currency] = deriveDecoder

  implicit val timestampEncoder: Encoder[Timestamp] = deriveEncoder
  implicit val timestampDecoder: Decoder[Timestamp] = deriveDecoder

  implicit val pairEncoder: Encoder[Pair] = deriveEncoder
  implicit val pairDecoder: Decoder[Pair] = deriveDecoder

  implicit val rateEncoder: Encoder[Rate] = deriveEncoder
  implicit val rateDecoder: Decoder[Rate] = deriveDecoder
}
