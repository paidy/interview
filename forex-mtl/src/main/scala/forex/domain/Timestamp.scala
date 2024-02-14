package forex.domain

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  def fromString(str: String): Timestamp = {
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val offsetDateTime = OffsetDateTime.parse(str, formatter)
    Timestamp(offsetDateTime)
  }
}
