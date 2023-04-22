package forex.http.external.oneframe

import forex.domain.model.Rate
import forex.http.external.oneframe.Protocol.RateResponse

trait OneFrameClient[F[_]] {
  def getRates(pairs: Seq[Rate.Pair]): F[List[RateResponse]]
}

