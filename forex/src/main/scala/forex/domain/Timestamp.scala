package forex.domain

import io.circe._
import io.circe.generic.extras.wrapped._
import io.circe.java8.time._
import java.time.OffsetDateTime

import scala.concurrent.duration.FiniteDuration

case class Timestamp(value: OffsetDateTime) extends AnyVal {

  def isNotOlderThan(than: FiniteDuration): Boolean = {
    value.isAfter(OffsetDateTime.now().minusNanos(than.toNanos))
  }
}

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  implicit val encoder: Encoder[Timestamp] =
    deriveUnwrappedEncoder[Timestamp]
}
