package forex.services.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[RateServiceError Either Rate]
}
