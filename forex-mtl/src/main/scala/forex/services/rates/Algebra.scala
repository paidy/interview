package forex.services.rates

import forex.domain.Rate
import forex.services.rates.errors.Error

trait Algebra[F[_]] {
  def get(pairs: List[Rate.Pair]): F[Error Either List[RateResponse]]
}
