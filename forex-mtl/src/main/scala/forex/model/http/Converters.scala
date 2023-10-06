package forex.model.http

import forex.model.domain.Rate

object Converters {

  import Protocol._

  implicit class GetApiResponseOps(val rate: Rate) extends AnyVal {
    def asGetApiResponse: GetApiResponse =
      GetApiResponse(
        from = rate.pair.from,
        to = rate.pair.to,
        price = rate.price,
        timestamp = rate.timestamp
      )
  }


  implicit class OneFrameRateOps(val rate: OneFrameRate) extends AnyVal {
    def asRate: Rate =
      Rate(
        pair = Rate.Pair(from = rate.from, to = rate.to),
        price = rate.price,
        timestamp = rate.timestamp
      )
  }
}
