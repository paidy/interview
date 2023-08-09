package forex.services.rates.interpreters

import cats.implicits._
import cats._
import forex.domain.Rate
import forex.services.storage
import forex.services.rates
import forex.services.rates.errors

class OneFrameLive[F[_]: Applicative](repo: storage.Algebra[F]) extends rates.Algebra[F] {
  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] =
    repo.get(pair).map(_.leftMap(e => errors.Error.OneFrameLookupFailed(e.msg)))
}
