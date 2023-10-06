package forex.services.rates

import fs2.Stream


trait Algebra[F[_]] {
  def ratesRefresh: Stream[F, Unit]
}
