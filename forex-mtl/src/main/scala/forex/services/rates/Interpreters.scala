package forex.services.rates

import cats.Applicative
import cats.effect.Sync
import forex.http.external.oneframe.OneFrameClient
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F]                    = new OneFrameDummy[F]()
  def http[F[_]: Sync](client: OneFrameClient[F]): Algebra[F] = new OneFrameImplOnMemory[F](client)
}
