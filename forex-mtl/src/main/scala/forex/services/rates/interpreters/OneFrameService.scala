package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.data.EitherT
import forex.domain.Rate
import forex.services
import forex.services.rates.errors._

class OneFrameService[F[_]: Applicative](ingestor: services.rates_ingestor.Algebra[F]) extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Error Either Rate] =
    EitherT(ingestor.get(pair)).leftMap(toRateServiceError).value
}
