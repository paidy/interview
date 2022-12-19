package forex.services.rates

import cats.Applicative
import cats.effect.Sync
import forex.config.OneFrameConfig
import interpreters._
import org.http4s.client.Client

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()

  def http[F[_]: Sync](
    cfg: OneFrameConfig,
    client: Client[F]
  ): Algebra[F] = new OneFrameHttp[F](cfg, client)
}
