package forex.services.rates_ingestor

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {

  def get(pair: Rate.Pair): F[Error Either Rate]

  def refreshCache: F[Unit]
}
