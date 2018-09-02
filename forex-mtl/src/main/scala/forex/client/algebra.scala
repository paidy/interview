package forex.client

import forex.domain.Rate

object algebra {

  trait OneForgeClient[F[_]] {
    def get(pair: Rate.Pair): F[Rate]
  }

}
