package forex.services.rates

import cats.Applicative
import interpreters._
import forex.services.storage

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()

  def live[F[_]: Applicative](repo: storage.Algebra[F]): Algebra[F] = new OneFrameLive[F](repo)
}
