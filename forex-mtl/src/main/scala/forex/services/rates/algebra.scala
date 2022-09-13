package forex.services.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
}

trait BatchedAlgebra[F[_]] {
  def get(pairs: Seq[Rate.Pair]): F[Error Either Seq[Rate]]
}
