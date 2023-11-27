package forex.repos.rates.interpreters
import forex.domain.{ Price, Rate, Timestamp }

object OneFrameConverter {
  private[interpreters] implicit class OneFrameResponseOps(val resp: OneFrameProtocol.OneFrameResponse) extends AnyVal {
    def toRates: Seq[Rate] = resp.rates.map { r =>
      Rate(
        pair = Rate.Pair(
          from = r.from,
          to = r.to
        ),
        price = Price(r.price),
        timestamp = Timestamp(r.time_stamp)
      )
    }
  }
}
