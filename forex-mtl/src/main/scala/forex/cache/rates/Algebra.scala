package forex.cache.rates

import forex.domain.Rate
import forex.programs.rates.errors.Error

trait Algebra[F[_]] {
  def update(ratePairs: Seq[Rate]): F[Error Either Unit]
  def get(ratePair: Rate.Pair): F[Error Either Rate]
}
