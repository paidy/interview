package forex.services.oneforge

import forex.domain._

trait Algebra[F[_]] {
  def get(pairs: Set[Rate.Pair]): F[Error Either Set[Rate]]
}
