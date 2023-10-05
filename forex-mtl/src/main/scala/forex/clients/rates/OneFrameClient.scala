package forex.clients.rates

import cats.Functor
import forex.clients.rates.Protocol.OneFrameRate
import forex.config.OneFrameClientConfig
import forex.domain.Rate
import forex.programs.rates.errors.Error


class OneFrameClient[F[_] : Functor](
                                      config: OneFrameClientConfig
                                    ) extends Algebra[F] {

  override def get(ratePairs: Set[Rate.Pair], token: String): F[Error Either Seq[OneFrameRate]] = ???

}


object OneFrameClient {

  def apply[F[_] : Functor](
                             config: OneFrameClientConfig
                           ): Algebra[F] = new OneFrameClient[F](config)
}
