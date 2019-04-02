package forex.services.rates

import forex.domain.Rate

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Rate]
}
