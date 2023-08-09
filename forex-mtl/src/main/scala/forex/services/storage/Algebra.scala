package forex.services.storage

import forex.domain.Rate

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[errors.Error Either Rate]
  def put(rate: Rate): F[errors.Error Either Unit]
}
