package forex.programs.rates

import forex.domain.Rate

trait Algebra[F[_]] {
  def get(request: Program.GetRatesRequest): F[Rate]
}

