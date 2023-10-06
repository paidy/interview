package forex.cache.rates

import forex.model.domain.Rate


trait Algebra[F[_]] {
  def update(ratePairs: Seq[Rate]): F[Unit]
  def get(ratePair: Rate.Pair): F[Rate]
}
