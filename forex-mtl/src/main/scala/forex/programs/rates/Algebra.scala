package forex.programs.rates

import forex.model.domain.Rate
import forex.model.http.Protocol

trait Algebra[F[_]] {
  def get(request: Protocol.GetApiRequest): F[Rate]
}
