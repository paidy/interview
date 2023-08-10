package forex.services.storage

import forex.domain.Rate

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Option[Rate]]
  def putAll(rates: List[Rate]): F[Unit]
}
