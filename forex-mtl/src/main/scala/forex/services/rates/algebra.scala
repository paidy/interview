package forex.services.rates

import cats.effect.Timer
import forex.domain.Rate
import errors._
import fs2.Stream

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
  def scheduleCacheRefresh()(implicit timer: Timer[F]): Stream[F, Unit]
}
