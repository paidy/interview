package forex.cache.rates

import cats.data.OptionT
import forex.model.domain.Rate


trait Algebra[F[_]] {
  def update(ratePairs: Seq[Rate]): F[Unit]
  def get(ratePair: Rate.Pair):  OptionT[F, Rate]
}
