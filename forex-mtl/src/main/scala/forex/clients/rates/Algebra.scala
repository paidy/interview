package forex.clients.rates

import Protocol.OneFrameRate
import forex.domain.Rate
import forex.programs.rates.errors.Error


trait Algebra[F[_]] {
  def get(ratePairs: Set[Rate.Pair], token: String): F[Error Either Seq[OneFrameRate]]
}

