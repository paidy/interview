package forex.cache

import cats.effect.Timer
import forex.domain.Rate
import fs2.Stream

trait Algebra[F[_]] {
  def getRate(key: String): F[Option[Rate]]
  def scheduleCacheRefresh()(implicit timer: Timer[F]): Stream[F, Unit]
}
