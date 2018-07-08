package forex.domain

import java.time.OffsetDateTime

import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

class TimestampSpec extends WordSpec with Matchers {

  "Timestamp" should {
    "allow to check if it's older than provided amount of time" in {
      Timestamp(OffsetDateTime.now().minusMinutes(4))
        .isNotOlderThan(5.minutes) shouldBe true

      Timestamp(OffsetDateTime.now().minusMinutes(1))
        .isNotOlderThan(2.minutes) shouldBe true

      Timestamp(OffsetDateTime.now().minusMinutes(6))
        .isNotOlderThan(5.minutes) shouldBe false

      Timestamp(OffsetDateTime.now().minusMinutes(7))
        .isNotOlderThan(6.minutes) shouldBe false
    }
  }
}
