package forex.services.rates

import cats.Applicative
import forex.client.OneFrameHttpClient
import interpreters._

object Interpreters {
  def oneFrameClient[F[_]: Applicative](oneFrameHttpClient: OneFrameHttpClient): Algebra[F] = new OneFrameClient[F](oneFrameHttpClient)
}
