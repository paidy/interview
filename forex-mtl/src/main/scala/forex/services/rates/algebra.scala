package forex.services.rates

import errors._
import forex.domain.model.Rate

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
}
