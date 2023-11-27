package forex.services.rates

import cats.Applicative
import forex.services
import interpreters._

object Interpreters {
  def apply[F[_]: Applicative](ingestor: services.rates_ingestor.Algebra[F]): Algebra[F] =
    new OneFrameService[F](ingestor)
}
