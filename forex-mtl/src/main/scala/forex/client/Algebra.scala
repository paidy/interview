package forex.client

import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.client.errors.Error

trait Algebra[F[_]] {
  def getRates(pairs: Vector[Pair]): F[Either[Error, List[Rate]]]
}
