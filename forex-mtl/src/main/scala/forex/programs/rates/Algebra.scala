package forex.programs.rates

import errors._
import forex.services.rates.RateResponse

trait Algebra[F[_]] {
  def get(request: Protocol.GetRatesRequest): F[Error Either List[RateResponse]]
}
