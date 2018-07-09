package forex.services.oneforge

import java.time.{Instant, OffsetDateTime, ZoneId}

import forex.domain.{Price, Rate, Timestamp}
import forex.services.oneforge.Error.UnparsableQuoteResponse
import io.circe._
import io.circe.generic.semiauto._
import cats.syntax.either._

case class OneForgeQuoteResponse(symbol: String, bid: Double, ask: Double, price: Double, timestamp: Long) {
  def toRate: Either[UnparsableQuoteResponse, Rate] =
    Rate.Pair.fromString(symbol).map(pair =>
      Rate(
        pair,
        Price(BigDecimal(price)),
        Timestamp(
          OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp),
            ZoneId.systemDefault()
          )
        )
      )
    ).leftMap(s => UnparsableQuoteResponse(s"Unable to parse symbol: $s"))
}

object OneForgeQuoteResponse {
  implicit val decoder: Decoder[OneForgeQuoteResponse] = deriveDecoder[OneForgeQuoteResponse]

}
