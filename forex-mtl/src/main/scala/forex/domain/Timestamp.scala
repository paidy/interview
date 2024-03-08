package forex.domain

import java.time.OffsetDateTime

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)
  
  def fromString(s: String): Timestamp = Timestamp(OffsetDateTime.parse(s))
}
