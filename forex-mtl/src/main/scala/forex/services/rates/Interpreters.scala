package forex.services.rates

import cats.Applicative
import forex.config.OneFrameConfig
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_] : Applicative](config: OneFrameConfig): Algebra[F] = new OneFrameLive[F](config)
}
