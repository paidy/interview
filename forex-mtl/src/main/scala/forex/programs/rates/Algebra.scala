package forex.programs.rates

import forex.programs._
import forex.domain.Rate

trait Algebra[F[_]] {
  def get(request: GetRatesRequest): F[Rate]
}

