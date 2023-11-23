package forex.http.rates

import forex.services.rates.RateResponse

object Converters {
  import Protocol._

  private[rates] implicit class GetApiResponseOps(val rate: RateResponse) extends AnyVal {
    def asGetApiResponse: GetApiResponse =
      GetApiResponse(
        from = rate.from,
        to = rate.to,
        price = rate.price,
        timestamp = rate.timeStamp
      )
  }

}
