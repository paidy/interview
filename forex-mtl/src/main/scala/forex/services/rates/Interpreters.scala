package forex.services.rates

import cats.{ Applicative, Monad }
import forex.persistence.RatesRepository
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative]: OneFrameAlgebra[F] = new OneFrameDummy[F]()

  def live[F[_]: Monad](repo: RatesRepository[F]): OneFrameAlgebra[F] = new OneFrameLive[F](repo)
}
