package forex.clients.rates

import forex.model.http.Protocol.OneFrameRate
import forex.model.domain.Rate


trait Algebra[F[_]] {
  def get(ratePairs: Set[Rate.Pair], token: String): F[Seq[OneFrameRate]]
}

