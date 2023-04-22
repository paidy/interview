package forex.services.rates.interpreters

import cats.effect.Sync
import cats.implicits.toFunctorOps
import forex.domain.model.Rate
import forex.http.external.oneframe.OneFrameClient
import forex.services.rates.Algebra
import forex.services.rates.errors._

class OneFrameImpl[F[_]: Sync](client: OneFrameClient[F]) extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Error Either Rate] = client.getRates(Seq(pair)).map {
    case Nil       => Left(Error.OneFrameLookupFailed("no pair"))
    case head :: _ => Right(head)
  }
}
