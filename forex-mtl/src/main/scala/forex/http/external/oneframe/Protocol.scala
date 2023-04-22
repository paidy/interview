package forex.http.external.oneframe

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

object Protocol {
  final case class RateResponse(
      from: String,
      to: String,
      bid: Double,
      ask: Double,
      price: Double,
      time_stamp: String
  )

  implicit val customConfig: Configuration =
    Configuration.default.withDefaults.withSnakeCaseMemberNames
  implicit val rateResponseDecoder: Decoder[RateResponse] = deriveConfiguredDecoder[RateResponse]
  implicit def rateResponseListEntityDecoder[F[_]: Sync]: EntityDecoder[F, List[RateResponse]] =
    jsonOf[F, List[RateResponse]]
}
