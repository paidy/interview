package forex.clients.rates

import cats.Functor
import forex.model.config.OneFrameClientConfig
import forex.model.http.Protocol.OneFrameRate
import forex.model.domain.Rate


class OneFrameClient[F[_] : Functor](
                                      config: OneFrameClientConfig
                                    ) extends Algebra[F] {

  override def get(ratePairs: Set[Rate.Pair], token: String): F[Seq[OneFrameRate]] = ???

}


object OneFrameClient {

  def apply[F[_] : Functor](
                             config: OneFrameClientConfig
                           ): Algebra[F] = new OneFrameClient[F](config)
}
