package forex.http.external.oneframe

import forex.domain.model.{ Currency, Price, Rate, Timestamp }
import forex.http.external.oneframe.Protocol.RateResponse

import java.time.OffsetDateTime

object Converters {

  private[oneframe] implicit class RateOps(val rateResponse: RateResponse) extends AnyVal {
    def asRate: Rate = Rate(
      pair = Rate.Pair(Currency.fromString(rateResponse.from), Currency.fromString(rateResponse.to)),
      price = Price(rateResponse.price),
      timestamp = Timestamp(OffsetDateTime.parse(rateResponse.time_stamp))
    )
  }
}
