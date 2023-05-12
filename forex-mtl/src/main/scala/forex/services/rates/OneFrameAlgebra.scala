package forex.services.rates

import forex.domain.Rate
import forex.programs.rates.errors.ForexError

trait OneFrameAlgebra[F[_]] {
  def get(pair: Rate.Pair): F[ForexError Either Rate]
}
