package forex.services.rates

import cats.Applicative
import cats.effect.Concurrent
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.interpreters._
import io.circe.generic.extras.JsonKey

import java.time.OffsetDateTime

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_]: Concurrent]: Algebra[F] = new OneFrameLive[F]()

  private[rates] implicit class OneFrameOps(val response: OneFrameResponse) extends AnyVal {
    def asRate: Rate =
      Rate(
        pair = Pair(
          from = response.from,
          to = response.to
        ),
        price = Price(response.price),
        timestamp = Timestamp(response.timestamp)
      )
  }

  final case class OneFrameResponse(
    val from: Currency,
    val to: Currency,
    val price: Double,
    @JsonKey("time_stamp") val timestamp: OffsetDateTime
  )
}
