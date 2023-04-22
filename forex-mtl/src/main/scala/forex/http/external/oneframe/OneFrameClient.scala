package forex.http.external.oneframe

import forex.domain.model.Rate

trait OneFrameClient[F[_]] {
  def getRates(pairs: Seq[Rate.Pair]): F[List[Rate]]
}
