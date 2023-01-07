package forex.services.rates

import cats.data.NonEmptyList
import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
  def getMany(pairs: NonEmptyList[Rate.Pair]): F[Error Either NonEmptyList[Rate]]
}
