package forex.programs.rates

import errors._
import forex.domain.model.Rate

trait Algebra[F[_]] {
  def get(request: Protocol.GetRatesRequest): F[Error Either Rate]
}
