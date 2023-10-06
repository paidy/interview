package forex.model.domain

import java.time.OffsetDateTime


final case class Timestamp(value: OffsetDateTime) extends AnyVal


object Timestamp {
  def now(): Timestamp =
    Timestamp(OffsetDateTime.now)
}
