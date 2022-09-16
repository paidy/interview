package forex.services.rates

import cats.Applicative
import cats.effect.Concurrent
import cats.effect.concurrent.{Ref, Semaphore}
import cats.syntax.all._
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.interpreters.OneFrameCached.CachedRate
import forex.services.rates.interpreters._
import io.circe.generic.extras.JsonKey

import java.time.OffsetDateTime

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_]: Concurrent]: Algebra[F] = new OneFrameLive[F](batched)
  def batched[F[_]: Concurrent]: BatchedAlgebra[F] = new OneFrameBatched[F]()
  def cached[F[_]: Concurrent]: F[Algebra[F]] = for {
    ref <- Ref.of[F, Map[Pair, CachedRate]](Map())
    sem <- Semaphore.apply(1)
    a = new OneFrameCached[F](
      batched,
      ref,
      sem
    )
  } yield (a)

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
