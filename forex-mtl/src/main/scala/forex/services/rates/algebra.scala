package forex.services.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
}

trait BatchAlgebra {
  def getBatch(pairs: Seq[Rate.Pair]): Either[Error, Seq[Rate]]
}